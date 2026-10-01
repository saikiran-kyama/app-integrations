package com.appintegrations.msg91.sms.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response from MSG91 SMS Report Logs API.
 *
 * <p>Example response:
 *
 * <pre>
 * {
 *     "data": [
 *         {
 *             "requestDate": "2026-05-07 12:48:56",
 *             "status": "Failed",
 *             "deliveryDate": "2026-05-07",
 *             "deliveryTime": "12:48:57",
 *             "telNum": "918309619653",
 *             "sentDateTime": "2026-05-07 12:48:56"
 *         }
 *     ],
 *     "metadata": {
 *         "total": 1,
 *         "paginationToken": "..."
 *     }
 * }
 * </pre>
 *
 * @see <a href="https://docs.msg91.com/reference/reports">MSG91 Reports API</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Msg91SmsLogResponse {

  /** List of SMS log entries. */
  @JsonProperty("data")
  private List<Msg91SmsLogEntry> data;

  /** Metadata containing pagination info. */
  @JsonProperty("metadata")
  private Metadata metadata;

  /** Error type if request failed. */
  @JsonProperty("type")
  private String type;

  /** Error message if request failed. */
  @JsonProperty("message")
  private String message;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Metadata {
    /** Total number of records. */
    @JsonProperty("total")
    private Integer total;

    /** Pagination token for fetching more results. */
    @JsonProperty("paginationToken")
    private String paginationToken;
  }

  /**
   * Check if the request was successful and has data.
   *
   * @return true if data is present with entries and no error
   */
  public boolean isSuccess() {
    return data != null && !data.isEmpty() && !"error".equalsIgnoreCase(type);
  }

  /**
   * Check if data was returned but is empty (no matching records).
   *
   * @return true if request succeeded but found no records
   */
  public boolean isEmptyResult() {
    return data != null && data.isEmpty() && !"error".equalsIgnoreCase(type);
  }

  /**
   * Check if there was an error.
   *
   * @return true if request failed
   */
  public boolean isError() {
    return "error".equalsIgnoreCase(type);
  }

  /**
   * Get error message or reason for no data.
   *
   * @return error message, empty result message, or null
   */
  public String getErrorMessage() {
    if (isError()) {
      return message;
    }
    if (data == null) {
      return "No data returned from API";
    }
    if (data.isEmpty()) {
      return "No matching records found for the given requestId";
    }
    return null;
  }

  /**
   * Get the first log entry status if available.
   *
   * @return status string or null
   */
  public String getFirstStatus() {
    if (data != null && !data.isEmpty()) {
      return data.get(0).getStatus();
    }
    return null;
  }

  /**
   * Get the first log entry if available.
   *
   * @return first entry or null
   */
  public Msg91SmsLogEntry getFirstEntry() {
    if (data != null && !data.isEmpty()) {
      return data.get(0);
    }
    return null;
  }

  /**
   * Creates an error response.
   *
   * @param errorMessage Error message
   * @return Error response
   */
  public static Msg91SmsLogResponse error(String errorMessage) {
    return Msg91SmsLogResponse.builder().type("error").message(errorMessage).build();
  }
}
