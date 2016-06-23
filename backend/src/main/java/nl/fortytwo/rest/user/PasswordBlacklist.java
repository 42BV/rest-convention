package nl.fortytwo.rest.user;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class PasswordBlacklist {

    private Set<String> blacklist = new HashSet<>();

    public PasswordBlacklist() throws IOException {
        load(getClass().getResourceAsStream("/top100-passwords.txt"));
    }

    private void load(InputStream src) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(src, Charset.forName("UTF-8")));
        try {
            String pwd = null;
            while ((pwd = in.readLine()) != null) {
                blacklist.add(pwd);
            }
        } finally {
            in.close();
        }
    }

    public boolean isBlacklisted(String pwd) {
        return blacklist.contains(pwd);
    }

}
