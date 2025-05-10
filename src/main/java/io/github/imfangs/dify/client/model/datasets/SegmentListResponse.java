package io.github.imfangs.dify.client.model.datasets;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


/**
 * 分段列表响应
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SegmentListResponse extends SegmentsCreateResponse {
    
    /**
     * 是否还有更多
     */
    private Boolean hasMore;

    /**
     * 总数量
     */
    private Integer total;

    /**
     * 当前页码
     */
    private Integer page;

    /**
     * 每页数量
     */
    private Integer limit;
}
