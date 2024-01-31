package selsup.test;

import com.google.gson.Gson;

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
    private final BlockingQueue<Long> requests;
    private final HttpClient client;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.timeUnit = timeUnit;
        this.requests = new LinkedBlockingQueue<>(requestLimit);
        this.client = HttpClient.newHttpClient();
    }

    private synchronized void allowRequest() throws InterruptedException {
        long currentTimeMillis = System.currentTimeMillis();
        long periodToCheck = timeUnit.toMillis(1);
        Long oldestRequest = requests.peek();
        while (oldestRequest != null && currentTimeMillis - oldestRequest > periodToCheck) {
            requests.remove();
            oldestRequest = requests.peek();
        }
        if (requests.remainingCapacity() == 0) {
            requests.take(); // blocks if queue is full
        }
        requests.put(currentTimeMillis);
    }

    public String createDocument(Document document, String signature) throws IOException, InterruptedException {
        allowRequest();

        Gson gson = new Gson();
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

        public Description description;
        public String doc_id;
        public String doc_status;
        public String doc_type;
        public boolean importRequest;
        public String owner_inn;
        public String participant_inn;
        public String producer_inn;
        public String production_date;
        public String production_type;
        public List<Product> products;
        public String reg_date;
        public String reg_number;

        static class Description {
            public String participantInn;
        }

        static class Product {
            public String certificate_document;
            public String certificate_document_date;
            public String certificate_document_number;
            public String owner_inn;
            public String producer_inn;
            public String production_date;
            public String tnved_code;
            public String uit_code;
            public String uitu_code;
        }
    }
}