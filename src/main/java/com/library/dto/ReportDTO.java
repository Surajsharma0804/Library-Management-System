package com.library.dto;

import java.util.List;

public class ReportDTO {
    private String title;
    private List<String> headers;
    private List<List<String>> rows;
    private String summary;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public List<String> getHeaders() { return headers; }
    public void setHeaders(List<String> headers) { this.headers = headers; }
    public List<List<String>> getRows() { return rows; }
    public void setRows(List<List<String>> rows) { this.rows = rows; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
}
