package nl.fortytwo.rest.security;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractHttpIntegrationTest {

    public static class Request {
        private String url;
        private HttpMethod method;
        private List<Header> headers = new ArrayList<>();
        private String body;

        public Request(String url, HttpMethod method) {
            this.url = url;
            this.method = method;
            this.headers.add(new BasicHeader("Origin", "https://localhost:8443"));
        }

        public Request(Response resp, String url, HttpMethod method) {
            this(url,method);
            for(Header h:resp.hdr) {
                if ("Set-Cookie".equals(h.getName())) {
                    addHeader("Cookie", h.getValue().substring(0, h.getValue().indexOf(";")));
                }
            }
        }
        
        public Request addHeader(String name, String value) {
            headers.add(new BasicHeader(name, value));
            return this;
        }

        public Request setBody(String body) {
            this.body = body;
            return this;
        }

        public <A> Request setBodyObject(A object) {
            try {
                setBody(new ObjectMapper().writeValueAsString(object));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            return this;
        }
    }

    public static class Response {
        private int status;
        private Header[] hdr;
        private String body;

        public Response(int status, Header[] hdr, String body) {
            this.status = status;
            this.hdr = hdr;
            this.body = body;
        }

        public int getStatus() {
            return status;
        }

        public boolean isOk() {
            return status == HttpStatus.OK.value();
        }

        public boolean isForbidden() {
            return status == HttpStatus.FORBIDDEN.value();
        }

        public boolean isUnauthorized() {
            return status == HttpStatus.UNAUTHORIZED.value();
        }

        public boolean isBadRequest() {
            return status == HttpStatus.BAD_REQUEST.value();
        }

        public String getHeader(String name) {
            for (Header h : hdr) {
                if (h.getName().equals(name)) {
                    return h.getValue();
                }
            }
            return null;
        }

        public boolean hasHeader(String name) {
            return getHeader(name) != null;
        }

        public boolean hasHeaderValue(String name, String value) {
            return value.equals(getHeader(name));
        }
        
        public String getCookie(String value) {
            for (Header h : hdr) {
                if (h.getName().equals("Set-Cookie")) {
                    if (h.getValue().startsWith(value)) {
                        return h.getValue();
                    }
                }
            }
            return null;
        }

        public String getXsrfToken() {
            String token = getCookie("XSRF-TOKEN");
            return token == null ? token : token.substring(token.indexOf('=')+1, token.indexOf(';'));
        }
        
        public String getBody() {
            return body;
        }

        public <A> A getBodyObject(Class<A> type) {
            try {
                return new ObjectMapper().readValue(body, type);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private CloseableHttpClient httpClient;
    private String baseUrl;

    public void init(String baseUrl) throws GeneralSecurityException {
        this.baseUrl = baseUrl;
        this.httpClient = createHttpClient();
    }

    public Response perform(Request req) throws ClientProtocolException, IOException {
        HttpRequestBase request = null;
        switch (req.method) {
        case GET:
            request = new HttpGet(baseUrl + req.url);
            break;
        case DELETE:
            request = new HttpDelete(baseUrl + req.url);
            break;
        case POST:
            request = new HttpPost(baseUrl + req.url);
            ((HttpPost) request).setEntity(new StringEntity(req.body));
            break;
        case PUT:
            request = new HttpPut(baseUrl + req.url);
            ((HttpPut) request).setEntity(new StringEntity(req.body));
            break;
        default:
            throw new RuntimeException(String.valueOf(req.method));
        }
        for (Header h : req.headers) {
            request.addHeader(h);
        }
        CloseableHttpResponse response = httpClient.execute(request);
        return handleResponse(response);
    }

    private Response handleResponse(CloseableHttpResponse response) throws IOException {
        try {
            String body = null;
            Header[] allHeaders = response.getAllHeaders();
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                body = EntityUtils.toString(entity);
            }
            LoggerFactory.getLogger(getClass()).info(String.valueOf(response.getStatusLine()));
            return new Response(response.getStatusLine().getStatusCode(), allHeaders, body);
        } finally {
            response.close();
        }
    }

    public CloseableHttpClient createHttpClient() throws GeneralSecurityException {
        HttpClientBuilder b = HttpClientBuilder.create();

        SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
            @Override
            public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                return true;
            }
        }).build();
        b.setSslcontext(sslContext);

        HostnameVerifier hostnameVerifier = SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
        Registry<ConnectionSocketFactory> socketFactoryRegistry = 
                RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory()).register("https", sslSocketFactory).build();

        PoolingHttpClientConnectionManager connMgr = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        b.setConnectionManager(connMgr);

        return b.build();
    }
}
