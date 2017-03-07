/**
 * Before run the code, please make sure your JDC lib has been installed 
 * "Java Cryptography Extension (JCE) Unlimited Strength". 
 * The library files should be located at $JDK_HOME/jre/lib/security/. 
 */

import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public final class AESCryptography {

	private static final String AES = "AES";
	private static final String SHA1PRNG = "SHA1PRNG";
	private static final String AES_CBC_PKCS5_PADDING = "AES/CBC/PKCS5Padding";
	private static final String PBKDF2_WITH_HMAC_SHA256 = "PBKDF2WithHmacSHA256";
	private static final int ITERATION_CNT = 65536;
	private static final int KEY_LENGTH = 256;

	public EncryptionMessage encrypt(String message) throws Exception {
		String salt = genSalt();

		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(PBKDF2_WITH_HMAC_SHA256);
		PBEKeySpec keySpec = new PBEKeySpec(message.toCharArray(),
										 salt.getBytes(),
										 ITERATION_CNT,
										 KEY_LENGTH);
		SecretKey secretKey = keyFactory.generateSecret(keySpec);
		SecretKeySpec secretSpec = new SecretKeySpec(secretKey.getEncoded(), AES);

		Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5_PADDING);
		cipher.init(Cipher.ENCRYPT_MODE, secretSpec);
		AlgorithmParameters params = cipher.getParameters();

		byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
		byte[] encryptedMsgBytes = cipher.doFinal(message.getBytes());
		EncryptionMessage result = new EncryptionMessage(iv, 
														 secretKey.getEncoded(),
														 Base64.getEncoder().encodeToString(encryptedMsgBytes));

		return result;
	}

	public String decrypt(EncryptionMessage encryptedMessage) {
		byte[] encryptedTextBytes = Base64.getDecoder().decode(encryptedMessage.getMessage());
		try {
			SecretKeySpec secretSpec = new SecretKeySpec(encryptedMessage.getKey(), AES);

			Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5_PADDING);
			cipher.init(Cipher.DECRYPT_MODE, secretSpec, new IvParameterSpec(encryptedMessage.getInitialVector()));

			byte[] decryptedTextBytes = cipher.doFinal(encryptedTextBytes);
			return new String(decryptedTextBytes);
		} catch (IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidKeyException | InvalidAlgorithmParameterException e) {
			throw new RuntimeException(e);
		}
	}

	private String genSalt() throws Exception {

		SecureRandom sr = SecureRandom.getInstance(SHA1PRNG);
		byte[] salt = new byte[20]; // 160 bits
		sr.nextBytes(salt);
		return new String(salt);
	}

	class EncryptionMessage {
		private byte[] initialVector;
		private byte[] key;
		private String message;

		public EncryptionMessage() {
		}

		public EncryptionMessage(byte[] initialVector, byte[] key, String message) {
			this.initialVector = initialVector;
			this.key = key;
			this.message = message;
		}

		public EncryptionMessage(String initialVector, String key, String message) {
			this.initialVector = Base64.getDecoder().decode(initialVector);
			this.key = Base64.getDecoder().decode(key);
			this.message = message;
		}

		public byte[] getInitialVector() {
			return initialVector;
		}

		public void setInitialVector(byte[] initialVector) {
			this.initialVector = initialVector;
		}

		public byte[] getKey() {
			return key;
		}

		public void setKey(byte[] key) {
			this.key = key;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public void setKey(String key) {
			this.key = Base64.getDecoder().decode(key);
		}

		public void setInitialVector(String initialVector) {
			this.initialVector = Base64.getDecoder().decode(initialVector);
		}

		public String displayKeyInBase64Text() {
			return Base64.getEncoder().encodeToString(key);
		}

		public String displayInitialVectorInBase64Text() {
			return Base64.getEncoder().encodeToString(initialVector);
		}

		@Override
		public String toString() {
			return "CryptoGraphyResult [initialVector=" + displayInitialVectorInBase64Text() + ", key="
					+ displayKeyInBase64Text() + ", message=" + message + "]";
		}
	}
	
	public static void main(String[] args) throws Exception {
		AESCryptography crypt = new AESCryptography();
		EncryptionMessage cryptObj = crypt.encrypt("HELLO NOte!!!");
		System.out.println(cryptObj.getMessage());
		String key = cryptObj.displayKeyInBase64Text();
		String iv = cryptObj.displayInitialVectorInBase64Text();
		String msg = cryptObj.getMessage();
		
		EncryptionMessage enc = new AESCryptography().new EncryptionMessage(iv, key, msg);
		System.out.println(crypt.decrypt(enc));

	}
}
