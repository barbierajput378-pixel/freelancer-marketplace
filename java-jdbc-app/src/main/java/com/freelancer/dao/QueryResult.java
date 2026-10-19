package com.freelancer.dao;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple carrier for an arbitrary tabular result (column names + rows), used by
 * the Dashboard so the UI can render any query in a JTable without knowing its
 * shape in advance.
 */
public class QueryResult {

    private final String[] columns;
    private final List<Object[]> rows;

    public QueryResult(String[] columns, List<Object[]> rows) {
        this.columns = columns;
        this.rows = rows;
    }

    public String[] getColumns() {
        return columns;
    }

    public Object[][] getRows() {
        return rows.toArray(new Object[0][]);
    }

    public int getRowCount() {
        return rows.size();
    }

    /** Reads an open ResultSet fully into a QueryResult (columns inferred from metadata). */
    public static QueryResult from(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int colCount = meta.getColumnCount();
        String[] cols = new String[colCount];
        for (int i = 0; i < colCount; i++) {
            cols[i] = meta.getColumnLabel(i + 1);
        }
        List<Object[]> data = new ArrayList<>();
        while (rs.next()) {
            Object[] row = new Object[colCount];
            for (int i = 0; i < colCount; i++) {
                row[i] = rs.getObject(i + 1);
            }
            data.add(row);
        }
        return new QueryResult(cols, data);
    }
}
