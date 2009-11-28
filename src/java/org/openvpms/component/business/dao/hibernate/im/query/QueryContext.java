package org.openvpms.component.business.dao.hibernate.im.query;

import org.apache.commons.lang.WordUtils;
import static org.openvpms.component.business.dao.hibernate.im.query.QueryBuilderException.ErrorCode.OperatorNotSupported;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.JoinConstraint;
import static org.openvpms.component.system.common.query.JoinConstraint.JoinType;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.RelationalOp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;


/**
 * This class holds the state of the HQL as it is being built.
 */
public class QueryContext {

    /**
     * The select clause.
     */
    private StringBuilder selectClause = new StringBuilder("select ");

    /**
     * The default select name, if none are specified.
     */
    private String defaultSelect;

    /**
     * The qualified names in the select clause.
     */
    private List<String> selectNames = new ArrayList<String>();

    /**
     * The names of the object reference being selected.
     */
    private List<String> refSelectNames = new ArrayList<String>();

    /**
     * The where clause part of the hql query
     */
    private Clause whereClause = new Clause();

    /**
     * The from clauses.
     */
    private List<FromClause> fromClauses = new ArrayList<FromClause>();

    /**
     * The ordered clause part of the hql query
     */
    private StringBuilder orderedClause = new StringBuilder(" order by ");

    /**
     * The initial order length.
     */
    private int initOrderedClauseLen = orderedClause.length();

    /**
     * A stack of types while processing the {@link ArchetypeQuery}.
     */
    private Stack<TypeSet> typeStack = new Stack<TypeSet>();

    /**
     * The types, keyed on alias.
     */
    private Map<String, TypeSet> typesets = new HashMap<String, TypeSet>();

    /**
     * Name allocator for types.
     */
    private NameAllocator typeNames = new NameAllocator();

    /**
     * Name allocator for parameters.
     */
    private NameAllocator paramNames = new NameAllocator();

    /**
     * a stack of parameters while processing the {@link ArchetypeQuery}.
     */
    private Stack<String> varStack = new Stack<String>();

    /**
     * A stack of join types.
     */
    private Stack<Counter<JoinConstraint.JoinType>> joinStack = new Stack<Counter<JoinConstraint.JoinType>>();

    /**
     * Holds a reference to the parameters and the values used to process
     */
    private Map<String, Object> params = new HashMap<String, Object>();


    /**
     * Default constructor.
     */
    QueryContext() {
        this(false);
    }

    /**
     * Constructs a <tt>QueryContext</tt>.
     *
     * @param distinct if <tt>true</tt> filter duplicate rows
     */
    QueryContext(boolean distinct) {
        if (distinct) {
            selectClause.append("distinct ");
        }
    }

    /**
     * Returns the HQL query string.
     *
     * @return the HQL query string
     */
    public String getQueryString() {
        StringBuilder result = new StringBuilder(selectClause);
        if (selectNames.isEmpty()) {
            result.append(defaultSelect);
        }
        if (!fromClauses.isEmpty()) {
            result.append(" from ");
            boolean first = true;
            for (FromClause from : fromClauses) {
                if (!first) {
                    if (from.needsComma()) {
                        result.append(", ");
                    } else {
                        result.append(" ");
                    }
                } else {
                    first = false;
                }
                result.append(from);
            }
        }
        if (!whereClause.isEmpty()) {
            result.append(" where ").append(whereClause);
        }
        if (orderedClause.length() != initOrderedClauseLen) {
            result.append(orderedClause);
        }
        return result.toString();
    }

    /**
     * Returns the query parameters.
     *
     * @return the query parameters
     */
    public Map<String, Object> getParameters() {
        return params;
    }

    /**
     * Returns the names in the select clause.
     *
     * @return the select clause names
     */
    public List<String> getSelectNames() {
        return selectNames;
    }

    /**
     * Returns the names of the object reference being selected.
     *
     * @return the object reference names
     */
    public List<String> getRefSelectNames() {
        return refSelectNames;
    }

    /**
     * Returns the types being selected, keyed on type alias.
     *
     * @return a map of type aliases to their corresponding short names
     */
    public Map<String, Set<String>> getSelectTypes() {
        Map<String, Set<String>> result = new HashMap<String, Set<String>>();
        for (String name : selectNames) {
            int index = name.indexOf(".");
            String alias;
            if (index == -1) {
                alias = name;
            } else {
                alias = name.substring(0, index);
            }
            if (result.get(alias) == null) {
                TypeSet set = typesets.get(alias);
                result.put(alias, set.getShortNames());
            }
        }
        return result;
    }

    /**
     * Push a logical operator on the stack
     *
     * @param op the operator to push
     * @return the operator context
     */
    OpState pushLogicalOperator(LogicalOperator op) {
        Clause clause = getClause();
        return new OpState(clause, clause.push(op));
    }

    public static class OpState {

        private final Clause clause;

        private final Counter<LogicalOperator> counter;

        private OpState(Clause clause, Counter<LogicalOperator> counter) {
            this.clause = clause;
            this.counter = counter;
        }

        public void pop() {
            clause.pop(counter);
        }
    }

    /**
     * Push the type set.
     *
     * @param types the type set
     * @return this context
     */
    QueryContext pushTypeSet(TypeSet types) {
        String alias = addTypeSet(types, types.getAlias());
        popJoin();


        boolean first = typeStack.isEmpty();
        FromClause fromClause;
        if (first) {
            fromClause = new FromClause(types.getClassName(), alias);
            defaultSelect = alias;
        } else {
            fromClause = new FromClause(JoinType.InnerJoin, types.getClassName(), alias);
        }

        fromClauses.add(fromClause);

        typeStack.push(types);
        varStack.push(alias);
        joinStack.push(new Counter<JoinType>(JoinType.InnerJoin));
        return this;
    }

    private void popJoin() {
        if (!joinStack.isEmpty()) {
            Clause clause = getFromClause();
            clause.popAll();
        }
    }

    /**
     * Push the distinct type given the specified joinType.
     *
     * @param types    the types to be pushed on the stack
     * @param property the property to join on
     * @param joinType the joinType to use
     * @return QueryContext
     */
    QueryContext pushTypeSet(TypeSet types, String property,
                             JoinConstraint.JoinType joinType) {
        popJoin();
        String alias = addTypeSet(types, property);
        FromClause fromClause = new FromClause(joinType, varStack.peek(), property, alias);
        fromClauses.add(fromClause);
        typeStack.push(types);
        varStack.push(alias);
        joinStack.push(new Counter<JoinType>(joinType));
        return this;
    }


    /**
     * Pop the logical operator on the stack
     *
     * @return TypeSet
     */
    TypeSet popTypeSet() {
        varStack.pop();
        joinStack.pop();
        return typeStack.pop();
    }

    /**
     * Look at the type that is currently on the stack
     *
     * @return TypeSet
     */
    TypeSet peekTypeSet() {
        return typeStack.peek();
    }

    /**
     * Look at the join type that is currently on the stack.
     *
     * @return the join type
     */
    JoinType peekJoinType() {
        return joinStack.peek().operator;
    }

    /**
     * Determines if the current constraint is an outer join.
     *
     * @return <tt>true</tt> if there is a join
     */
    boolean isOuterJoin() {
        return !joinStack.isEmpty() && joinStack.peek().operator != JoinType.InnerJoin;
    }

    /**
     * Returns the type associated with an alias, or the type on type top
     * of the stack if the alias is null.
     *
     * @param alias the type alias. May be <tt>null</tt>
     * @return the associated result set or <tt>null</tt> if none is found
     */
    TypeSet getTypeSet(String alias) {
        TypeSet result = null;
        if (alias != null) {
            result = typesets.get(alias);
        } else if (!typeStack.isEmpty()) {
            result = typeStack.peek();
        }
        return result;
    }

    /**
     * Push a variable name on the stack
     *
     * @param varName the variable name to push
     * @return this context
     */
    QueryContext pushVariable(String varName) {
        varStack.push(varName);
        return this;
    }

    /**
     * Pop the variable name on the stack
     *
     * @return String
     */
    String popVariable() {
        return varStack.pop();
    }

    /**
     * Adds a select constraint.
     *
     * @param alias    the type alias. May be <tt>null</tt>
     * @param node     the node name. May be <tt>null</tt>
     * @param property the property. May be <tt>null</tt>
     */
    void addSelectConstraint(String alias, String node, String property) {
        if (alias == null) {
            alias = varStack.peek();
        }
        if (!selectNames.isEmpty()) {
            selectClause.append(", ");
        }

        selectClause.append(alias);
        if (property != null) {
            selectClause.append('.');
            selectClause.append(property);
        }
        if (node == null) {
            selectNames.add(alias);
        } else {
            selectNames.add(alias + "." + node);
        }
    }

    /**
     * Adds an object reference select constraint.
     *
     * @param alias    the type alias. May be <tt>null</tt>
     * @param nodeName the node name. May ve <tt>null</tt>
     */
    void addObjectRefSelectConstraint(String alias, String nodeName) {
        if (alias == null) {
            alias = varStack.peek();
        }
        if (!selectNames.isEmpty()) {
            selectClause.append(", ");
        }

        String prefix = (nodeName != null) ? alias + "." + nodeName : alias;
        selectClause.append(prefix);
        selectClause.append(".archetypeId, ");
        selectClause.append(prefix);
        selectClause.append(".id, ");
        selectClause.append(prefix);
        selectClause.append(".linkId");

        selectNames.add(prefix + ".archetypeId");
        selectNames.add(prefix + ".id");
        selectNames.add(prefix + ".linkId");
        refSelectNames.add(prefix);
    }

    /**
     * Adds a where constraint.
     *
     * @param alias    the type alias. May be <tt>null</tt>
     * @param property the property
     * @param op       the relational operator to apply
     * @param value    the object value
     * @return this context
     */
    QueryContext addWhereConstraint(String alias, String property,
                                    RelationalOp op, Object value) {
        if (alias == null) {
            alias = varStack.peek();
        }
        addWhereConstraint(alias + "." + property, op, value);
        return this;
    }

    /**
     * Adds a where constraint.
     *
     * @param property the fully qualified property
     * @param op       the relational operator to apply
     * @param value    the object value
     * @return this context
     */
    QueryContext addWhereConstraint(String property, RelationalOp op, Object value) {
        whereClause.appendOperator();
        whereClause.append(property)
                .append(" ")
                .append(getOperator(op, value));

        // check if we need a parameter
        if (value != null) {
            String varName = paramNames.getName(property);
            whereClause.append(" :")
                    .append(varName);
            params.put(varName, getValue(op, value));
        }
        return this;
    }

    QueryContext addWithConstraint(String alias, String property, RelationalOp op, Object value) {
        if (alias == null) {
            alias = varStack.peek();
        }
        addWithConstraint(alias + "." + property, op, value);
        return this;
    }

    QueryContext addWithConstraint(String property, RelationalOp op, Object value) {
        Clause fromClause = getFromClause();
        fromClause.appendOperator();
        fromClause.append(property)
                .append(" ")
                .append(getOperator(op, value));

        // check if we need a parameter
        if (value != null) {
            String varName = paramNames.getName(property);
            fromClause.append(" :").append(varName);
            params.put(varName, getValue(op, value));
        }
        return this;
    }

    void addConstraint(String alias, String property, RelationalOp op, Object value) {
        if (isOuterJoin()) {
            addWithConstraint(alias, property, op, value);
        } else {
            addWhereConstraint(alias, property, op, value);
        }
    }

    void addConstraint(String property, RelationalOp op, Object value) {
        if (isOuterJoin()) {
            addWithConstraint(property, op, value);
        } else {
            addWhereConstraint(property, op, value);
        }
    }

    /**
     * Add the specified sort constraint.
     *
     * @param alias     the type alias. May be <tt>null</tt>
     * @param property  the property to be sorted.
     * @param ascending whether it is ascending or not
     * @return this context
     */
    QueryContext addSortConstraint(String alias, String property,
                                   boolean ascending) {
        if (orderedClause.length() > initOrderedClauseLen) {
            orderedClause.append(", ");
        }
        if (alias == null) {
            alias = varStack.peek();
        }
        orderedClause.append(alias)
                .append(".")
                .append(property);
        if (ascending) {
            orderedClause.append(" asc");
        } else {
            orderedClause.append(" desc");
        }

        return this;
    }

    /**
     * Add a where constraint given the property and the node constraint.
     *
     * @param property   the attribute to constrain
     * @param constraint the node constraint
     * @return this context
     */
    QueryContext addNodeConstraint(String property, NodeConstraint constraint) {
        Clause clause = getClause();
        String varName;
        RelationalOp op = constraint.getOperator();
        String qname = getQualifiedPropertyName(property);
        Object[] parameters = constraint.getParameters();
        switch (op) {
            case BTW:
                if (parameters[0] != null || parameters[1] != null) {
                    // process left hand side
                    Counter<LogicalOperator> state = clause.push(LogicalOperator.And);
                    if (parameters[0] != null) {
                        clause.appendOperator();
                        varName = paramNames.getName(property);
                        clause.append(qname)
                                .append(getOperator(RelationalOp.GTE, parameters[0]))
                                .append(" :")
                                .append(varName);
                        params.put(varName, getValue(RelationalOp.GTE, parameters[0]));
                    }

                    // process right hand side
                    if (parameters[1] != null) {
                        clause.appendOperator();
                        varName = paramNames.getName(property);
                        clause.append(qname)
                                .append(getOperator(RelationalOp.LTE, parameters[1]))
                                .append(" :")
                                .append(varName);
                        params.put(varName, getValue(RelationalOp.LTE, parameters[1]));
                    }
                    clause.pop(state);
                }
                break;

            case EQ:
            case GT:
            case GTE:
            case LT:
            case LTE:
            case NE:
                clause.appendOperator();

                varName = paramNames.getName(property);
                clause.append(qname)
                        .append(" ")
                        .append(getOperator(op, parameters[0]))
                        .append(" :")
                        .append(varName);
                params.put(varName, getValue(op, parameters[0]));
                break;

            case IsNULL:
            case IS_NULL:
            case NOT_NULL:
                clause.appendOperator();
                String opNull = " " + getOperator(op, null);
                if (isReference(property)) {
                    clause.append(qname).append(".id").append(opNull);
                } else {
                    clause.append(qname).append(opNull);
                }
                break;

            case IN:
                clause.appendOperator();
                boolean ref = isReference(property);
                clause.append(qname);
                if (ref) {
                    clause.append(".id");
                }
                clause.append(" ")
                        .append(getOperator(op, null))
                        .append(" (");

                for (int i = 0; i < parameters.length; ++i) {
                    if (i > 0) {
                        clause.append(", ");
                    }
                    varName = paramNames.getName(property);
                    clause.append(":").append(varName);
                    params.put(varName, getValue(op, parameters[i]));
                }
                clause.append(")");
                break;
            default:
                throw new QueryBuilderException(OperatorNotSupported, op);
        }
        return this;
    }

    /**
     * Adds a constraint between two properties.
     *
     * @param lhs      the left-hand property. Must be fully qualified
     * @param operator the operator
     * @param rhs      the right hand property. Must be fully qualified
     * @return this context
     */
    QueryContext addPropertyConstraint(String lhs, RelationalOp operator, String rhs) {
        Clause clause = getClause();
        switch (operator) {
            case EQ:
            case GT:
            case GTE:
            case LT:
            case LTE:
            case NE:
                clause.appendOperator();
                clause.append(lhs)
                        .append(" ")
                        .append(getOperator(operator, rhs))
                        .append(" ")
                        .append(rhs);
                break;
            default:
                throw new QueryBuilderException(OperatorNotSupported, operator);
        }

        return this;
    }

    /**
     * Return the HQL operator.
     *
     * @param operator the operator type
     * @param param    the value to associated with the operator
     * @return String
     *         the fragement
     */
    private String getOperator(RelationalOp operator, Object param) {
        switch (operator) {
            case EQ:
                if (param instanceof String) {
                    String sparam = (String) param;
                    if (sparam.contains("%") || sparam.contains("*")) {
                        return "like";
                    }
                }
                return "=";
            case GT:
                return ">";
            case GTE:
                return ">=";
            case LT:
                return "<";
            case LTE:
                return "<=";
            case NE:
                return "!=";
            case IsNULL:
            case IS_NULL:
                return "is NULL";
            case NOT_NULL:
                return "is NOT NULL";
            case IN:
                return "in";
            default:
                throw new QueryBuilderException(OperatorNotSupported, operator);
        }
    }

    /**
     * Returns the value.
     *
     * @param operator the operator type
     * @param param    the value to associated with the operator
     * @return the value
     */
    private Object getValue(RelationalOp operator, Object param) {
        switch (operator) {
            case EQ:
                if (param instanceof String) {
                    return ((String) param).replace("*", "%");
                }
                return param;

            default:
                if (param instanceof IMObjectReference) {
                    return ((IMObjectReference) param).getId();
                }
                return param;
        }
    }

    /**
     * Qualifies a property with the current alias.
     *
     * @param property the property
     * @return the qualified property
     */
    private String getQualifiedPropertyName(String property) {
        int index = property.indexOf('.');
        if (index == -1) {
            return varStack.peek() + "." + property;
        }
        String prefix = property.substring(0, index);
        if (typesets.get(prefix) == null) {
            return varStack.peek() + "." + property;
        }
        return property;
    }

    /**
     * Adds  a type set, creating an alias for it if one is not specified
     *
     * @param types the type set
     * @param alias an alias for the type if it doesn't have one. May be <tt>null</tt>
     * @return the type set's alias
     */
    private String addTypeSet(TypeSet types, String alias) {
        if (types.getAlias() != null) {
            typeNames.reserve(types.getAlias());
            alias = types.getAlias();
        } else {
            if (alias == null) {
                alias = types.getClassName();
            }
            alias = typeNames.getName(alias);
            types.setAlias(alias);
        }
        typesets.put(types.getAlias(), types);
        return alias;
    }

    /**
     * Determines if a property is an object reference.
     *
     * @param property the property
     * @return <tt>true</tt> if the property is an object reference
     */
    private boolean isReference(String property) {
        int index = property.indexOf('.');
        String node;
        String alias;
        if (index == -1) {
            alias = varStack.peek();
            node = property;
        } else {
            alias = property.substring(0, index);
            node = property.substring(index + 1);
        }
        TypeSet set = getTypeSet(alias);
        if (set == null) {
            return false;
        }
        for (ArchetypeDescriptor archetype : set.getDescriptors()) {
            NodeDescriptor descriptor = archetype.getNodeDescriptor(node);
            if (descriptor == null || !descriptor.isObjectReference()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the current clause.
     *
     * @return the current clause
     */
    private Clause getClause() {
        return (isOuterJoin()) ? getFromClause() : whereClause;
    }

    /**
     * Returns the current from clause.
     *
     * @return the current from clause
     */
    private FromClause getFromClause() {
        return fromClauses.get(fromClauses.size() - 1);
    }

    /**
     * This is used to track the logical operator.
     */
    enum LogicalOperator {

        And(" and "),
        Or(" or ");

        /**
         * Holds the string value.
         */
        private String value;

        /**
         * Constructor that takes string value.
         *
         * @param value the operator
         */
        LogicalOperator(String value) {
            this.value = value;
        }

        /**
         * Return the value.
         *
         * @return String
         */
        public String getValue() {
            return value;
        }
    }

    private static class Clause {

        StringBuilder clause = new StringBuilder();

        Stack<Counter<LogicalOperator>> stack = new Stack<Counter<LogicalOperator>>();

        public Counter<LogicalOperator> push(LogicalOperator operator) {
            appendOperator();
            Counter<LogicalOperator> result = new Counter<LogicalOperator>(operator);
            stack.push(result);
            append("(");
            return result;
        }

        public void pop(Counter<LogicalOperator> state) {
            if (stack.contains(state)) {
                Counter<LogicalOperator> top = null;
                while (top != state) {
                    top = stack.pop();
                    append(")");
                }
            }
        }

        public void popAll() {
            if (!stack.isEmpty()) {
                pop(stack.lastElement());
            }
        }

        /**
         * Append the logical operator if required.
         */
        public void appendOperator() {
            if (!stack.isEmpty()) {
                if (stack.peek().count > 0) {
                    String op = stack.peek().operator.getValue();
                    clause.append(op);
                }
                stack.peek().count++;
            }
        }

        public boolean isEmpty() {
            return clause.length() == 0;
        }

        public String toString() {
            return clause.toString();
        }

        public Clause append(String value) {
            clause.append(value);
            return this;
        }
    }

    private static class FromClause extends Clause {

        private boolean with;

        private final boolean needsComma;

        public FromClause(String type, String alias) {
            this(null, type, alias);

        }

        public FromClause(JoinType join, String type, String alias) {
            this(join, null, type, alias);
        }

        public FromClause(JoinType join, String variable, String property, String alias) {
            if (join == JoinType.InnerJoin && variable == null) {
                needsComma = true;
            } else {
                needsComma = false;
                appendJoin(join);
            }
            if (variable != null) {
                super.append(variable);
                super.append(".");
            }
            super.append(property);
            super.append(" as ");
            super.append(alias);
            with = false;
        }

        public boolean needsComma() {
            return needsComma;
        }

        private void appendJoin(JoinType join) {
            if (join == JoinType.InnerJoin) {
                super.append("inner join ");
            } else if (join == JoinType.LeftOuterJoin) {
                super.append("left outer join ");
            } else if (join == JoinType.RightOuterJoin) {
                super.append("right outer join ");
            }
        }

        public Clause append(String value) {
            if (!with) {
                super.append(" with ");
                with = true;
            }
            super.append(value);
            return this;
        }
    }

    private static class Counter<T> {

        /**
         * The operator.
         */
        T operator;

        /**
         * Counts the number of terms applied to this operator.
         */
        int count;

        /**
         * Create an instance using the specified operator.
         *
         * @param operator the operator
         */
        Counter(T operator) {
            this.operator = operator;
        }
    }

    private static class NameAllocator {

        private Set<String> names = new HashSet<String>();

        public void reserve(String name) {
            names.add(name);
        }

        public String getName(String name) {
            int index = name.lastIndexOf(".");
            if (index != -1) {
                name = name.substring(index + 1);
            }
            if (name.endsWith("DO")) {
                // strip off the DO suffix to make generated HQL a little
                // easier to read
                name = name.substring(0, name.length() - 2);
            }
            name = WordUtils.uncapitalize(name);
            int i = 0;
            String result = name + i;
            while (names.contains(result)) {
                ++i;
                result = name + i;
            }
            names.add(result);
            return result;
        }

    }

}