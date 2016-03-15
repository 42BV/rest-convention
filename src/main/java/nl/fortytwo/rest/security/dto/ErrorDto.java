package nl.fortytwo.rest.security.dto;

public class ErrorDto {
    
    private String error;

    public ErrorDto(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }
}
