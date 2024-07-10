package com.s1lkpay.silkauthorizationserver;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class CrptApi {

    private final TimeUnit timeUnit;
    private final int requestLimit;
    private final Semaphore semaphore;
    private final ScheduledExecutorService scheduler;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiUrl = "https://ismp.crpt.ru/api/v3/lk/documents/create";

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.timeUnit = timeUnit;
        this.requestLimit = requestLimit;
        this.semaphore = new Semaphore(requestLimit);
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();

        long delay = timeUnit.toMillis(1);
        scheduler.scheduleAtFixedRate(() -> semaphore.release(requestLimit - semaphore.availablePermits()), delay, delay, TimeUnit.MILLISECONDS);
    }

    public void createDocumentForIntroduction(Document document, String signature) throws InterruptedException, IOException {
        semaphore.acquire();

        ObjectNode json = objectMapper.createObjectNode();
        json.set("description", objectMapper.valueToTree(document.getDescription()));
        json.put("doc_id", document.getDocId());
        json.put("doc_status", document.getDocStatus());
        json.put("doc_type", "LP_INTRODUCE_GOODS");
        json.put("importRequest", document.isImportRequest());
        json.put("owner_inn", document.getOwnerInn());
        json.put("participant_inn", document.getParticipantInn());
        json.put("producer_inn", document.getProducerInn());
        json.put("production_date", document.getProductionDate());
        json.put("production_type", document.getProductionType());
        json.set("products", objectMapper.valueToTree(document.getProducts()));
        json.put("reg_date", document.getRegDate());
        json.put("reg_number", document.getRegNumber());

        String requestBody = objectMapper.writeValueAsString(json);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("Signature", signature)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to create document: " + response.body());
        }
    }

    public static class Document {
        private Description description;
        private String docId;
        private String docStatus;
        private boolean importRequest;
        private String ownerInn;
        private String participantInn;
        private String producerInn;
        private String productionDate;
        private String productionType;
        private Product[] products;
        private String regDate;
        private String regNumber;

        public Description getDescription() {
            return description;
        }

        public void setDescription(Description description) {
            this.description = description;
        }

        public String getDocId() {
            return docId;
        }

        public void setDocId(String docId) {
            this.docId = docId;
        }

        public String getDocStatus() {
            return docStatus;
        }

        public void setDocStatus(String docStatus) {
            this.docStatus = docStatus;
        }

        public boolean isImportRequest() {
            return importRequest;
        }

        public void setImportRequest(boolean importRequest) {
            this.importRequest = importRequest;
        }

        public String getOwnerInn() {
            return ownerInn;
        }

        public void setOwnerInn(String ownerInn) {
            this.ownerInn = ownerInn;
        }

        public String getParticipantInn() {
            return participantInn;
        }

        public void setParticipantInn(String participantInn) {
            this.participantInn = participantInn;
        }

        public String getProducerInn() {
            return producerInn;
        }

        public void setProducerInn(String producerInn) {
            this.producerInn = producerInn;
        }

        public String getProductionDate() {
            return productionDate;
        }

        public void setProductionDate(String productionDate) {
            this.productionDate = productionDate;
        }

        public String getProductionType() {
            return productionType;
        }

        public void setProductionType(String productionType) {
            this.productionType = productionType;
        }

        public Product[] getProducts() {
            return products;
        }

        public void setProducts(Product[] products) {
            this.products = products;
        }

        public String getRegDate() {
            return regDate;
        }

        public void setRegDate(String regDate) {
            this.regDate = regDate;
        }

        public String getRegNumber() {
            return regNumber;
        }

        public void setRegNumber(String regNumber) {
            this.regNumber = regNumber;
        }
    }

    public static class Description {
        private String participantInn;

        public String getParticipantInn() {
            return participantInn;
        }

        public void setParticipantInn(String participantInn) {
            this.participantInn = participantInn;
        }
    }

    public static class Product {
        private String certificateDocument;
        private String certificateDocumentDate;
        private String certificateDocumentNumber;
        private String ownerInn;
        private String producerInn;
        private String productionDate;
        private String tnvedCode;
        private String uitCode;
        private String uituCode;

        public String getCertificateDocument() {
            return certificateDocument;
        }

        public void setCertificateDocument(String certificateDocument) {
            this.certificateDocument = certificateDocument;
        }

        public String getCertificateDocumentDate() {
            return certificateDocumentDate;
        }

        public void setCertificateDocumentDate(String certificateDocumentDate) {
            this.certificateDocumentDate = certificateDocumentDate;
        }

        public String getCertificateDocumentNumber() {
            return certificateDocumentNumber;
        }

        public void setCertificateDocumentNumber(String certificateDocumentNumber) {
            this.certificateDocumentNumber = certificateDocumentNumber;
        }

        public String getOwnerInn() {
            return ownerInn;
        }

        public void setOwnerInn(String ownerInn) {
            this.ownerInn = ownerInn;
        }

        public String getProducerInn() {
            return producerInn;
        }

        public void setProducerInn(String producerInn) {
            this.producerInn = producerInn;
        }

        public String getProductionDate() {
            return productionDate;
        }

        public void setProductionDate(String productionDate) {
            this.productionDate = productionDate;
        }

        public String getTnvedCode() {
            return tnvedCode;
        }

        public void setTnvedCode(String tnvedCode) {
            this.tnvedCode = tnvedCode;
        }

        public String getUitCode() {
            return uitCode;
        }

        public void setUitCode(String uitCode) {
            this.uitCode = uitCode;
        }

        public String getUituCode() {
            return uituCode;
        }

        public void setUituCode(String uituCode) {
            this.uituCode = uituCode;
        }
    }
}
