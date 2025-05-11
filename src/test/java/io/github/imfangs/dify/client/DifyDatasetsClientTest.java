package io.github.imfangs.dify.client;

import io.github.imfangs.dify.client.config.DifyTestConfig;
import io.github.imfangs.dify.client.exception.DifyApiException;
import io.github.imfangs.dify.client.model.DifyConfig;
import io.github.imfangs.dify.client.model.datasets.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Dify 知识库客户端测试类
 * 注意：运行测试前，请确保已经正确配置了 dify-test-config.properties 文件
 */
public class DifyDatasetsClientTest {
    private static final String BASE_URL = DifyTestConfig.getBaseUrl();
    private static final String API_KEY = DifyTestConfig.getDatasetsApiKey();
    private static final String USER_ID = "test-user-" + System.currentTimeMillis();

    private DifyDatasetsClient datasetsClient;
    private String testDatasetId;
    private String testDocumentId;

    @BeforeEach
    public void setUp() {
        // 创建客户端
        datasetsClient = DifyClientFactory.createDatasetsClient(BASE_URL, API_KEY);

        // 创建测试知识库
        try {
            CreateDatasetRequest createRequest = CreateDatasetRequest.builder()
                    .name("测试知识库-" + System.currentTimeMillis())
                    .description("这是一个测试知识库")
                    .indexingTechnique("high_quality")
                    .permission("only_me")
                    .provider("vendor")
                    .build();

            DatasetResponse response = datasetsClient.createDataset(createRequest);
            testDatasetId = response.getId();
            System.out.println("创建测试知识库成功，ID: " + testDatasetId);
        } catch (Exception e) {
            System.err.println("创建测试知识库失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @AfterEach
    public void tearDown() {
        // 清理测试数据
        if (testDatasetId != null) {
            try {
                datasetsClient.deleteDataset(testDatasetId);
                System.out.println("删除测试知识库成功，ID: " + testDatasetId);
            } catch (Exception e) {
                System.err.println("删除测试知识库失败: " + e.getMessage());
            }
        }
    }

    /**
     * 测试创建知识库
     */
    @Test
    public void testCreateDataset() throws IOException, DifyApiException {
        // 创建知识库请求
        CreateDatasetRequest request = CreateDatasetRequest.builder()
                .name("测试知识库-" + System.currentTimeMillis())
                .description("这是一个通过测试创建的知识库")
                .indexingTechnique("high_quality")
                .permission("only_me")
                .provider("vendor")
                .build();

        // 发送请求
        DatasetResponse response = datasetsClient.createDataset(request);

        // 验证响应
        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals(request.getName(), response.getName());
        assertEquals(request.getDescription(), response.getDescription());

        // 清理测试数据
        datasetsClient.deleteDataset(response.getId());
    }

    /**
     * 测试获取知识库列表
     */
    @Test
    public void testGetDatasets() throws IOException, DifyApiException {
        // 获取知识库列表
        DatasetListResponse response = datasetsClient.getDatasets(1, 10);

        // 验证响应
        assertNotNull(response);
        assertNotNull(response.getData());
        assertNotNull(response.getTotal());

        // 打印知识库列表
        System.out.println("知识库总数: " + response.getTotal());
        response.getData().forEach(dataset -> {
            System.out.println("知识库ID: " + dataset.getId() + ", 名称: " + dataset.getName());
        });
    }

    /**
     * 测试通过文本创建文档
     */
    @Test
    public void testCreateDocumentByText() throws IOException, DifyApiException {
        // 跳过测试如果没有测试知识库
        if (testDatasetId == null) {
            System.out.println("跳过测试，因为没有测试知识库");
            return;
        }

        RetrievalModel retrievalModel = new RetrievalModel();
        retrievalModel.setSearchMethod("hybrid_search");
        retrievalModel.setRerankingEnable(false);
        retrievalModel.setTopK(2);
        retrievalModel.setScoreThresholdEnabled(false);

        // 创建文档请求
        CreateDocumentByTextRequest request = CreateDocumentByTextRequest.builder()
                .name("测试文档-" + System.currentTimeMillis())
                .text("这是一个测试文档的内容。\n这是第二行内容。\n这是第三行内容。")
                .indexingTechnique("economy")
                .docForm("text_model")
                // 1.1.3 invalid_param (400) - Must not be null! 【doc_language】
                .docLanguage("Chinese")
                // 1.1.3 invalid_param (400) - Must not be null! 【retrieval_model】
                .retrievalModel(retrievalModel)
                // 没有这里的设置，会500报错，服务器内部错误
                .processRule(ProcessRule.builder().mode("automatic").build())
                .build();

        // 发送请求
        DocumentResponse response = datasetsClient.createDocumentByText(testDatasetId, request);

        // 验证响应
        assertNotNull(response);
        assertNotNull(response.getDocument());
        assertNotNull(response.getDocument().getId());
        assertEquals(request.getName(), response.getDocument().getName());

        // 保存文档ID用于后续测试
        testDocumentId = response.getDocument().getId();
        System.out.println("创建测试文档成功，ID: " + testDocumentId);
    }

    /**
     * 测试通过文件创建文档
     */
    @Test
    public void testCreateDocumentByFile() throws IOException {
        // 跳过测试如果没有测试知识库
        if (testDatasetId == null) {
            System.out.println("跳过测试，因为没有测试知识库");
            return;
        }

        File file = new File("/tmp/file.txt");
        if (!file.exists()) {
            System.out.println("文件不存在，跳过测试");
            return;
        }

        RetrievalModel retrievalModel = new RetrievalModel();
        retrievalModel.setSearchMethod("hybrid_search");
        retrievalModel.setRerankingEnable(false);
        retrievalModel.setTopK(2);
        retrievalModel.setScoreThresholdEnabled(false);

        // 创建文档请求
        CreateDocumentByFileRequest request = CreateDocumentByFileRequest.builder()
                .indexingTechnique("economy")
                .docForm("text_model")
                // 1.1.3 invalid_param (400) - Must not be null! 【doc_language】
                .docLanguage("Chinese")
                // 1.1.3 invalid_param (400) - Must not be null! 【retrieval_model】
                .retrievalModel(retrievalModel)
                // 没有这里的设置，会500报错，服务器内部错误
                .processRule(ProcessRule.builder().mode("automatic").build())
                .build();

        // 发送请求
        DocumentResponse response = datasetsClient.createDocumentByFile(testDatasetId, request, file);

        // 验证响应
        assertNotNull(response);
        assertNotNull(response.getDocument());
        assertNotNull(response.getDocument().getId());

        // 保存文档ID用于后续测试
        testDocumentId = response.getDocument().getId();
        System.out.println("创建测试文档成功，ID: " + testDocumentId);
    }

    /**
     * 测试获取文档列表
     */
    @Test
    public void testGetDocuments() throws IOException, DifyApiException {
        // 跳过测试如果没有测试知识库
        if (testDatasetId == null) {
            System.out.println("跳过测试，因为没有测试知识库");
            return;
        }

        // 获取文档列表
        DocumentListResponse response = datasetsClient.getDocuments(testDatasetId, null, 1, 10);

        // 验证响应
        assertNotNull(response);
        assertNotNull(response.getData());
        assertNotNull(response.getTotal());

        // 打印文档列表
        System.out.println("文档总数: " + response.getTotal());
        response.getData().forEach(document -> {
            System.out.println("文档ID: " + document.getId() + ", 名称: " + document.getName());
        });
    }

    /**
     * 测试删除文档
     */
    @Test
    public void testDeleteDocument() throws IOException, DifyApiException {
        // 跳过测试如果没有测试知识库或文档
        if (testDatasetId == null ) {
            System.out.println("跳过测试，因为没有测试知识库");
            return;
        }

        // 先创建一个文档
        if (testDocumentId == null) {
            createTestDocument();
        }

        // 删除文档
        datasetsClient.deleteDocument(testDatasetId, testDocumentId);

        // 清除文档ID
        testDocumentId = null;
    }

    /**
     * 测试检索知识库
     */
    @Test
    public void testRetrieveDataset() throws IOException, DifyApiException {
        // 跳过测试如果没有测试知识库
        if (testDatasetId == null) {
            System.out.println("跳过测试，因为没有测试知识库");
            return;
        }

        // 先创建一个文档
        if (testDocumentId == null) {

            RetrievalModel retrievalModel = new RetrievalModel();
            retrievalModel.setSearchMethod("hybrid_search");
            retrievalModel.setRerankingEnable(false);
            retrievalModel.setTopK(2);
            retrievalModel.setScoreThresholdEnabled(false);

            CreateDocumentByTextRequest createRequest = CreateDocumentByTextRequest.builder()
                    .name("检索测试文档-" + System.currentTimeMillis())
                    .text("人工智能（Artificial Intelligence，简称AI）是计算机科学的一个分支，它企图了解智能的实质，并生产出一种新的能以人类智能相似的方式做出反应的智能机器。人工智能是对人的意识、思维的信息过程的模拟。人工智能不是人的智能，但能像人那样思考、也可能超过人的智能。")
                    .indexingTechnique("high_quality")
                    .docForm("text_model")
                    // 1.1.3 invalid_param (400) - Must not be null! 【doc_language】
                    .docLanguage("Chinese")
                    // 1.1.3 invalid_param (400) - Must not be null! 【retrieval_model】
                    .retrievalModel(retrievalModel)
                    // 没有这里的设置，会500报错，服务器内部错误
                    .processRule(ProcessRule.builder().mode("automatic").build())
                    .build();

            DocumentResponse docResponse = datasetsClient.createDocumentByText(testDatasetId, createRequest);
            testDocumentId = docResponse.getDocument().getId();

            // 等待索引完成
            try {
                System.out.println("等待文档索引完成...");
                Thread.sleep(5000); // 等待5秒
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // 创建检索请求
        RetrievalModel retrievalModel = new RetrievalModel();
        retrievalModel.setTopK(3);
        retrievalModel.setScoreThreshold(0.5f);

        RetrieveRequest request = RetrieveRequest.builder()
                .query("什么是人工智能")
                .retrievalModel(retrievalModel)
                .build();

        // 发送请求
        RetrieveResponse response = datasetsClient.retrieveDataset(testDatasetId, request);

        // 验证响应
        assertNotNull(response);
        assertNotNull(response.getQuery());
        assertNotNull(response.getRecords());

        // 打印检索结果
        System.out.println("检索查询: " + response.getQuery().getContent());
        System.out.println("检索结果数量: " + response.getRecords().size());
        response.getRecords().forEach(record -> {
            System.out.println("分数: " + record.getScore());
            System.out.println("内容: " + record.getSegment().getContent());
            System.out.println("文档: " + record.getSegment().getDocument().getName());
        });
    }

    /**
     * 测试自定义配置
     */
    @Test
    public void testCustomConfig() throws IOException, DifyApiException {
        // 创建自定义配置
        DifyConfig config = DifyConfig.builder()
                .baseUrl(BASE_URL)
                .apiKey(API_KEY)
                .connectTimeout(5000)
                .readTimeout(60000)
                .writeTimeout(30000)
                .build();

        // 使用自定义配置创建客户端
        DifyDatasetsClient customClient = DifyClientFactory.createDatasetsClient(config);

        // 获取知识库列表
        DatasetListResponse response = customClient.getDatasets(1, 5);

        // 验证响应
        assertNotNull(response);
        assertNotNull(response.getData());
    }

    /**
     * 测试新增修改删除元数据
     */
    @Test
    public void testMetadata() throws IOException, DifyApiException {
        // 跳过测试如果没有测试知识库
        if (testDatasetId == null) {
            System.out.println("跳过测试，因为没有测试知识库");
            return;
        }

        // 创建元数据请求
        CreateMetadataRequest createRequest = CreateMetadataRequest.builder().name("test").type("string").build();

        // 发送请求
        MetadataResponse createResponse = datasetsClient.createMetadata(testDatasetId, createRequest);

        // 验证响应
        assertNotNull(createResponse);
        assertNotNull(createResponse.getId());
        assertNotNull(createResponse.getType());
        assertNotNull(createResponse.getName());
        assertEquals(createRequest.getName(), createResponse.getName());

        // 更新元数据
        UpdateMetadataRequest updateRequest = UpdateMetadataRequest.builder().name("test2").build();
        MetadataResponse updateResponse = datasetsClient.updateMetadata(testDatasetId, createResponse.getId(), updateRequest);
        assertEquals(updateRequest.getName(), updateResponse.getName());

        // 删除元数据
        String deleteResponse = datasetsClient.deleteMetadata(testDatasetId, createResponse.getId());
        // 验证响应
        assertNotNull(deleteResponse);
    }

    /**
     * 测试更新文档元数据
     */
    @Test
    public void testUpdateDocumentMetadata() throws IOException, DifyApiException {
        // 跳过测试如果没有测试知识库
        if (testDatasetId == null) {
            System.out.println("跳过测试，因为没有测试知识库");
            return;
        }

        // 先创建一个文档
        if (testDocumentId == null) {
            createTestDocument();
        }

        // 创建元数据
        CreateMetadataRequest createRequest = CreateMetadataRequest.builder()
                .name("test_metadata")
                .type("string")
                .build();

        MetadataResponse metadataResponse = datasetsClient.createMetadata(testDatasetId, createRequest);
        assertNotNull(metadataResponse);
        assertNotNull(metadataResponse.getId());

        // 创建更新文档元数据请求
        UpdateDocumentMetadataRequest.Metadata metadata = UpdateDocumentMetadataRequest.Metadata.builder()
                .id(metadataResponse.getId())
                .value("test_value")
                .name(metadataResponse.getName())
                .build();

        UpdateDocumentMetadataRequest.OperationData operationData = UpdateDocumentMetadataRequest.OperationData.builder()
                .documentId(testDocumentId)
                .metadataList(java.util.Collections.singletonList(metadata))
                .build();

        System.out.println("operationData: " + operationData);

        UpdateDocumentMetadataRequest request = UpdateDocumentMetadataRequest.builder()
                .operationData(java.util.Collections.singletonList(operationData))
                .build();

        // 发送请求
        String response = datasetsClient.updateDocumentMetadata(testDatasetId, request);

        // 验证响应
        assertNotNull(response);
        System.out.println("更新文档元数据成功: " + response);
    }

    /**
     * 测试创建分段
     */
    @Test
    public void testCreateSegments() throws IOException, DifyApiException {
        // 跳过测试如果没有测试知识库
        if (testDatasetId == null) {
            System.out.println("跳过测试，因为没有测试知识库");
            return;
        }

        // 先创建一个文档
        if (testDocumentId == null) {
            createTestDocument();
        }

        // 创建分段请求
        CreateSegmentsRequest.SegmentInfo segment = CreateSegmentsRequest.SegmentInfo.builder()
                .content("这是一个测试分段内容")
                .build();

        CreateSegmentsRequest request = CreateSegmentsRequest.builder()
                .segments(java.util.Collections.singletonList(segment))
                .build();

        // 发送请求
        SegmentListResponse response = datasetsClient.createSegments(testDatasetId, testDocumentId, request);

        // 验证响应
        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
        assertNotNull(response.getData().get(0).getId());

        // 保存segmentId到类属性，用于后续测试
        String testSegmentId = response.getData().get(0).getId();
        System.out.println("创建测试分段成功，ID: " + testSegmentId);
    }

    /**
     * 测试获取分段列表
     */
    @Test
    public void testGetSegments() throws IOException, DifyApiException {
        // 跳过测试如果没有测试知识库
        if (testDatasetId == null) {
            System.out.println("跳过测试，因为没有测试知识库");
            return;
        }

        // 先创建一个文档
        if (testDocumentId == null) {
            createTestDocument();
        }

        // 先创建一个分段
        String testSegmentId = createTestSegment(); // 调用测试方法来创建分段
        if (testSegmentId == null) {
            System.out.println("创建分段失败，跳过测试");
            return;
        }
        
        // 获取分段列表
        SegmentListResponse response = datasetsClient.getSegments(testDatasetId, testDocumentId, null, null, 1, 10);

        // 验证响应
        assertNotNull(response);
        assertNotNull(response.getData());

        // 打印分段列表
        System.out.println("分段数量: " + response.getData().size());
        response.getData().forEach(segment -> {
            System.out.println("分段ID: " + segment.getId() + ", 内容: " + segment.getContent());
        });
    }

    /**
     * 测试更新分段
     */
    @Test
    public void testUpdateSegment() throws IOException, DifyApiException {
        // 跳过测试如果没有测试知识库
        if (testDatasetId == null) {
            System.out.println("跳过测试，因为没有测试知识库");
            return;
        }

        // 先创建一个文档
        if (testDocumentId == null) {
            createTestDocument();
        }

        // 创建一个分段并获取ID
        String segmentId = createTestSegment();
        if (segmentId == null) {
            System.out.println("创建分段失败，跳过测试");
            return;
        }

        // 更新分段请求
        UpdateSegmentRequest request = UpdateSegmentRequest.builder()
                .segment(UpdateSegmentRequest.SegmentInfo.builder()
                        .content("这是更新后的分段内容")
                        .keywords(java.util.Collections.singletonList("这是一个要点"))
                        .regenerateChildChunks(true)
                        .build())
                .build();

        // 发送请求
        SegmentResponse response = datasetsClient.updateSegment(testDatasetId, testDocumentId, segmentId, request);

        // 验证响应
        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals(request.getSegment().getContent(), response.getData().getContent());
    }

    /**
     * 测试创建子块
     */
    @Test
    public void testCreateChildChunks() throws IOException, DifyApiException {
        // 跳过测试如果没有测试知识库
        if (testDatasetId == null) {
            System.out.println("跳过测试，因为没有测试知识库");
            return;
        }

        // 先创建一个文档
        if (testDocumentId == null) {
            createTestDocument();
        }

        // 创建一个分段并获取ID
        String testSegmentId = createTestSegment(); 
        if (testSegmentId == null) {
            System.out.println("创建分段失败，跳过测试");
            return;
        }
        

        // 创建子块请求
        SaveChildChunkRequest request = SaveChildChunkRequest.builder()
                .content("这是一个测试子块内容")
                .build();

        // 发送请求
        ChildChunkResponse response = datasetsClient.createChildChunk(testDatasetId, testDocumentId, testSegmentId, request);

        // 验证响应
        assertNotNull(response);
        assertNotNull(response.getData());
        assertNotNull(response.getData().getId());

        // 保存子块ID到类属性，用于后续测试
        String testChildChunkId = response.getData().getId();
        System.out.println("创建测试子块成功，ID: " + testChildChunkId);
    }

    /**
     * 测试获取子块列表
     */
    @Test
    public void testGetChildChunks() throws IOException, DifyApiException {
        // 跳过测试如果没有测试知识库
        if (testDatasetId == null) {
            System.out.println("跳过测试，因为没有测试知识库");
            return;
        }

        // 先创建一个文档
        if (testDocumentId == null) {
            createTestDocument();
        }

        // The segment ID to use for the test
        String segmentId = createTestSegment();
        if (segmentId == null) {
            System.out.println("创建分段失败，跳过测试");
            return;
        }

        // Create a child chunk for testing
        createTestChildChunk(segmentId);

        // 获取子块列表
        ChildChunkListResponse response = datasetsClient.getChildChunks(testDatasetId, testDocumentId, segmentId, null, 1, 10);

        // 验证响应
        assertNotNull(response);
        assertNotNull(response.getData());

        System.out.println("子块数量: " + response.getData().size());
        response.getData().forEach(childChunk -> {
            System.out.println("子块ID: " + childChunk.getId() + ", 内容: " + childChunk.getContent());
        });
    }

    /**
     * 测试更新子块
     */
    @Test
    public void testUpdateChildChunks() throws IOException, DifyApiException {
        // 跳过测试如果没有测试知识库
        if (testDatasetId == null) {
            System.out.println("跳过测试，因为没有测试知识库");
            return;
        }

        // 先创建一个文档
        if (testDocumentId == null) {
            createTestDocument();
        }

        // 创建一个分段并获取ID
        String segmentId = createTestSegment();
        if (segmentId == null) {
            System.out.println("创建分段失败，跳过测试");
            return;
        }

        // 创建一个子块并获取ID
        String childChunkId = createTestChildChunk(segmentId);
        if (childChunkId == null) {
            System.out.println("创建子块失败，跳过测试");
            return;
        }

        // 更新子块请求
        SaveChildChunkRequest request = SaveChildChunkRequest.builder()
                .content("这是更新后的子块内容")
                .build();

        // 发送请求
        ChildChunkResponse response = datasetsClient.updateChildChunk(testDatasetId, testDocumentId, segmentId, childChunkId, request);

        // 验证响应
        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals(request.getContent(), response.getData().getContent());
    }

    /**
     * 测试删除子块
     */
    @Test
    public void testDeleteChildChunks() throws IOException, DifyApiException {
        // 跳过测试如果没有测试知识库
        if (testDatasetId == null) {
            System.out.println("跳过测试，因为没有测试知识库");
            return;
        }

        // 先创建一个文档
        if (testDocumentId == null) {
            createTestDocument();
        }

        // 创建一个分段并获取ID
        String segmentId = createTestSegment();
        if (segmentId == null) {
            System.out.println("创建分段失败，跳过测试");
            return;
        }

        // 创建一个子块并获取ID
        String childChunkId = createTestChildChunk(segmentId);
        if (childChunkId == null) {
            System.out.println("创建子块失败，跳过测试");
            return;
        }

        // 删除子块
        datasetsClient.deleteChildChunks(testDatasetId, testDocumentId, segmentId, childChunkId);
        
        // 验证删除成功 - 获取子块列表并检查被删除的子块是否不存在
        ChildChunkListResponse response = datasetsClient.getChildChunks(testDatasetId, testDocumentId, segmentId, null, 1, 10);
        assertNotNull(response);
        
        // 检查删除的子块不在返回列表中
        boolean childChunkExists = response.getData().stream()
                .anyMatch(chunk -> childChunkId.equals(chunk.getId()));
        assertEquals(false, childChunkExists, "子块应该已被删除");
    }

    /**
     * 测试删除分段
     */
    @Test
    public void testDeleteSegment() throws IOException, DifyApiException {
        // 跳过测试如果没有测试知识库
        if (testDatasetId == null) {
            System.out.println("跳过测试，因为没有测试知识库");
            return;
        }

        // 先创建一个文档
        if (testDocumentId == null) {
            createTestDocument();
        }

        // 创建一个分段并获取ID
        String segmentId = createTestSegment();
        if (segmentId == null) {
            System.out.println("创建分段失败，跳过测试");
            return;
        }

        // 删除分段
        datasetsClient.deleteSegment(testDatasetId, testDocumentId, segmentId);
        
        // 验证删除成功 - 获取分段列表并检查被删除的分段是否不存在
        SegmentListResponse response = datasetsClient.getSegments(testDatasetId, testDocumentId, null, null, 1, 10);
        assertNotNull(response);
        
        // 检查删除的分段不在返回列表中
        boolean segmentExists = response.getData().stream()
                .anyMatch(segment -> segmentId.equals(segment.getId()));
        assertEquals(false, segmentExists, "分段应该已被删除");

    }

    /**
     * 创建测试文档
     * @return 文档ID
     */
    private String createTestDocument() throws IOException, DifyApiException {
        RetrievalModel retrievalModel = new RetrievalModel();
        retrievalModel.setSearchMethod("hybrid_search");
        retrievalModel.setRerankingEnable(false);
        retrievalModel.setTopK(2);
        retrievalModel.setScoreThresholdEnabled(false);

        CreateDocumentByTextRequest request = CreateDocumentByTextRequest.builder()
                .name("测试文档-" + System.currentTimeMillis())
                .text("这是一个测试文档的内容。\n这是第二行内容。\n这是第三行内容。")
                .indexingTechnique("economy")
                .docForm("text_model")
                .docLanguage("Chinese")
                .retrievalModel(retrievalModel)
                .processRule(ProcessRule.builder().mode("automatic").build())
                .build();

        DocumentResponse response = datasetsClient.createDocumentByText(testDatasetId, request);
        testDocumentId = response.getDocument().getId();
        System.out.println("创建测试文档成功，ID: " + testDocumentId);

        // 等待索引完成
        try {
            System.out.println("等待文档索引完成...");
            Thread.sleep(5000); // 等待5秒
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
            
        return testDocumentId;
    }

    /**
     * 创建测试分段
     * @return 分段ID
     */
    private String createTestSegment() throws IOException, DifyApiException {
        CreateSegmentsRequest.SegmentInfo segment = CreateSegmentsRequest.SegmentInfo.builder()
                .content("这是一个测试分段内容")
                .build();

        CreateSegmentsRequest request = CreateSegmentsRequest.builder()
                .segments(java.util.Collections.singletonList(segment))
                .build();

        SegmentListResponse response = datasetsClient.createSegments(testDatasetId, testDocumentId, request);
        
        if (response != null && response.getData() != null) {
            String segmentId = response.getData().get(0).getId();
            System.out.println("创建测试分段成功，ID: " + segmentId);
            return segmentId;
        }
        
        return null;
    }

    /**
     * 创建测试子块
     * @param segmentId 分段ID
     * @return 子块ID
     */
    private String createTestChildChunk(String segmentId) throws IOException, DifyApiException {
        SaveChildChunkRequest request = SaveChildChunkRequest.builder()
                .content("这是一个测试子块内容")
                .build();

        ChildChunkResponse response = datasetsClient.createChildChunk(testDatasetId, testDocumentId, segmentId, request);
        
        if (response != null && response.getData() != null) {
            String childChunkId = response.getData().getId();
            System.out.println("创建测试子块成功，ID: " + childChunkId);
            return childChunkId;
        }
        
        return null;
    }
}
