package fr.frogdevelopment.nihongo.billing;

public enum FIXME {

    DEVELOPER_PAYLOAD("bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ"),
    /**
     * fixme :
     * base64EncodedPublicKey should be YOUR APPLICATION'S PUBLIC KEY
     * (that you got from the Google Play developer console). This is not your
     * developer public key, it's the *app-specific* public key.
     * <p>
     * Instead of just storing the entire literal string here embedded in the
     * program,  construct the key at runtime from pieces or
     * use bit manipulation (for example, XOR with some other string) to hide
     * the actual key.  The key itself is not secret information, but we don't
     * want to make it easy for an attacker to replace the public key with one
     * of their own and then fake messages from the server.
     */
    PUBLIC_KEY("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsp1jGc8Qjp0I/gi1CQDac4BlGa5NFUwLV1N3GYIZ8WHUbytlvYssYQWT9Mz7bMYb0NcAGgivACMUZWZGeHdwcfppzGgegjnwmaJyC3X+bhUtRBQpLDM4wUl62PvCxukBpRJI/iBQZzciU9teEBMMMEjqHbHloK6z7qPDI7NsaCAP+vGarSICx9UBABgj/OPz4YDX3UcBGM49XTVKSB6Xo7j3TXeYC/LptXZSXG1RXTMQyt5O/ZwvTgG71C+KkzHv70K/7+JfdRS0DKkjSfvtw8YYOWJJE6O1ZeoaE2useOBHb7Z+RpggQEeRAt63kvC/p/X4JKz6YFeukoIdwQrzAwIDAQAB");

    public final String value;

    FIXME(String value) {
        this.value = value;
    }
}
