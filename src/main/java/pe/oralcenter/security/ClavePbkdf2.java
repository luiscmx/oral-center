package pe.oralcenter.security;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Hash con sal aleatoria, PBKDF2-HMAC-SHA256 y 600 000 iteraciones. */
public class ClavePbkdf2 {
    private static final int ITERACIONES = 600_000;
    private final SecureRandom aleatorio = new SecureRandom();

    public String crearHash(char[] clave) {
        if (clave == null || clave.length == 0 || clave.length > 1024) {
            throw new IllegalArgumentException("La contraseña debe tener entre 1 y 1024 caracteres.");
        }
        byte[] sal = new byte[16];
        aleatorio.nextBytes(sal);
        return "pbkdf2-sha256$" + ITERACIONES + "$" + Base64.getEncoder().encodeToString(sal)
                + "$" + Base64.getEncoder().encodeToString(derivar(clave, sal, ITERACIONES));
    }
    public boolean verificar(char[] clave, String hash) {
        if (clave == null || clave.length == 0 || clave.length > 1024 || hash == null) {
            return false;
        }
        byte[] calculado = null;
        try {
            String[] partes = hash.split("\\$", -1);
            if (partes.length != 4 || !partes[0].equals("pbkdf2-sha256")) {
                return false;
            }
            int iteraciones = Integer.parseInt(partes[1]);
            if (iteraciones < ITERACIONES || iteraciones > 2_000_000) {
                return false;
            }
            byte[] sal = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            if (sal.length != 16 || esperado.length != 32) {
                return false;
            }
            calculado = derivar(clave, sal, iteraciones);
            return MessageDigest.isEqual(esperado, calculado);
        } catch (IllegalArgumentException ex) {
            return false;
        } finally {
            if (calculado != null) {
                Arrays.fill(calculado, (byte) 0);
            }
        }
    }
    private byte[] derivar(char[] clave, byte[] sal, int iteraciones) {
        PBEKeySpec especificacion = new PBEKeySpec(clave, sal, iteraciones, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(especificacion).getEncoded();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("No se pudo verificar el acceso.", ex);
        } finally {
            especificacion.clearPassword();
        }
    }
}
