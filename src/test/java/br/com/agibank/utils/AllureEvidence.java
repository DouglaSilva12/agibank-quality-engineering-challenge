package br.com.agibank.utils;

import io.qameta.allure.Allure;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;

public final class AllureEvidence {

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private AllureEvidence() {
    }

    public static void anexarImagemPorUrl(String nomeEvidencia, String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            Allure.addAttachment(nomeEvidencia + " - URL nao retornada", "text/plain",
                    "A API nao retornou URL de imagem para anexar.");
            return;
        }

        Allure.addAttachment(nomeEvidencia + " - URL", "text/plain", imageUrl);

        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(imageUrl))
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();

            HttpResponse<byte[]> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                Allure.addAttachment(nomeEvidencia + " - falha no download", "text/plain",
                        "HTTP " + response.statusCode() + " ao baixar a imagem: " + imageUrl);
                return;
            }

            String contentType = response.headers()
                    .firstValue("content-type")
                    .orElseGet(() -> contentTypePorExtensao(imageUrl));

            Allure.addAttachment(
                    nomeEvidencia,
                    contentType,
                    new ByteArrayInputStream(response.body()),
                    extensaoPorContentType(contentType, imageUrl)
            );
        } catch (Exception exception) {
            Allure.addAttachment(nomeEvidencia + " - falha no anexo", "text/plain",
                    "Nao foi possivel anexar a imagem " + imageUrl + System.lineSeparator()
                            + exception.getClass().getSimpleName() + ": " + exception.getMessage());
        }
    }

    private static String contentTypePorExtensao(String imageUrl) {
        String lowerUrl = imageUrl.toLowerCase(Locale.ROOT);

        if (lowerUrl.endsWith(".png")) {
            return "image/png";
        }

        if (lowerUrl.endsWith(".gif")) {
            return "image/gif";
        }

        return "image/jpeg";
    }

    private static String extensaoPorContentType(String contentType, String imageUrl) {
        String lowerContentType = contentType.toLowerCase(Locale.ROOT);

        if (lowerContentType.contains("png")) {
            return ".png";
        }

        if (lowerContentType.contains("gif")) {
            return ".gif";
        }

        if (imageUrl.toLowerCase(Locale.ROOT).endsWith(".jpeg")) {
            return ".jpeg";
        }

        return ".jpg";
    }
}
