package io.github.imfangs.dify.client.model.datasets;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDocumentMetadataRequest {
  private List<OperationData> operationData;


  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor 
  public static class OperationData {
    private String documentId;
    private List<Metadata> metadataList;
  }


  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Metadata {
    private String id;
    private String name;
    private String value;
  }
}
