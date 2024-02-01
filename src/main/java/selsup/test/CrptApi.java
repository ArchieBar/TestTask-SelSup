package selsup.test;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class CrptApi {
    private final TimeUnit timeUnit;
    private transient final BlockingQueue<Long> requests; // Очередь для хранения времени каждого запроса
    private final HttpClient client;
    private final Gson gson;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.timeUnit = timeUnit;
        this.requests = new LinkedBlockingQueue<>(requestLimit); // Инициализация очереди с заданным лимитом
        this.client = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    // Метод для проверки, не превышено ли число запросов в единицу времени
    private void isRequestNumberExeeded() throws InterruptedException {
        long currentTimeMillis = System.currentTimeMillis();
        long periodToCheck = timeUnit.toMillis(1);
        Long oldestRequest;

        synchronized (requests) {
            oldestRequest = requests.peek();
            while (oldestRequest != null && currentTimeMillis - oldestRequest > periodToCheck) {
                requests.remove();
                oldestRequest = requests.peek();
            }
            if (requests.remainingCapacity() == 0) {
                throw new InterruptedException("Rate limit exceeded");
            }
            requests.put(currentTimeMillis); // Добавление времени запроса в очередь
        }
    }

    public String createDocument(Document document, String signature)
            throws IOException, InterruptedException, JsonSyntaxException {

        isRequestNumberExeeded(); // Проверка, не превышено ли число запросов

        String documentJson = gson.toJson(document);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://ismp.crpt.ru/api/v3/lk/documents/create"))
                .header("Content-Type", "application/json")
                .header("Signature", signature)
                .POST(HttpRequest.BodyPublishers.ofString(documentJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }

    static class Document {

        private Description description;
        @SerializedName("doc_id")
        private String docId;
        @SerializedName("doc_status")
        private String docStatus;
        @SerializedName("doc_type")
        private String docType;
        private boolean importRequest;
        @SerializedName("owner_inn")
        private String ownerInn;
        @SerializedName("participant_inn")
        private String participantInn;
        @SerializedName("producer_inn")
        private String producerInn;
        @SerializedName("production_date")
        private String productionDate;
        @SerializedName("production_type")
        private String productionType;
        private List<Product> products;
        @SerializedName("reg_date")
        private String regDate;
        @SerializedName("reg_number")
        private String regNumber;

        public Document(
                Description description,
                String docId,
                String docStatus,
                String docType,
                boolean importRequest,
                String ownerInn,
                String participantInn,
                String producerInn,
                String productionDate,
                String productionType,
                List<Product> products,
                String regDate,
                String regNumber) {

            this.description = description;
            this.docId = docId;
            this.docStatus = docStatus;
            this.docType = docType;
            this.importRequest = importRequest;
            this.ownerInn = ownerInn;
            this.participantInn = participantInn;
            this.producerInn = producerInn;
            this.productionDate = productionDate;
            this.productionType = productionType;
            this.products = products;
            this.regDate = regDate;
            this.regNumber = regNumber;
        }

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

        public String getDocType() {
            return docType;
        }

        public void setDocType(String docType) {
            this.docType = docType;
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

        public List<Product> getProducts() {
            return products;
        }

        public void setProducts(List<Product> products) {
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

        static class Description {
            private String participantInn;

            public Description(String participantInn) {
                this.participantInn = participantInn;
            }

            public String getParticipantInn() {
                return participantInn;
            }

            public void setParticipantInn(String participantInn) {
                this.participantInn = participantInn;
            }
        }

        static class Product {
            @SerializedName("certificate_document")
            private String certificateDocument;
            @SerializedName("certificate_document_date")
            private String certificateDocumentDate;
            @SerializedName("certificate_document_number")
            private String certificateDocumentNumber;
            @SerializedName("owner_inn")
            private String ownerInn;
            @SerializedName("producer_inn")
            private String producerInn;
            @SerializedName("production_date")
            private String productionDate;
            @SerializedName("tnved_code")
            private String tnvedCode;
            @SerializedName("uit_code")
            private String uitCode;
            @SerializedName("uitu_code")
            private String uituCode;

            public Product(
                    String certificateDocument,
                    String certificateDocumentDate,
                    String certificateDocumentNumber,
                    String ownerInn,
                    String producerInn,
                    String productionDate,
                    String tnvedCode,
                    String uitCode,
                    String uituCode) {

                this.certificateDocument = certificateDocument;
                this.certificateDocumentDate = certificateDocumentDate;
                this.certificateDocumentNumber = certificateDocumentNumber;
                this.ownerInn = ownerInn;
                this.producerInn = producerInn;
                this.productionDate = productionDate;
                this.tnvedCode = tnvedCode;
                this.uitCode = uitCode;
                this.uituCode = uituCode;
            }

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
}