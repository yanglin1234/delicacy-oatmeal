package com.delicacy.oatmeal.common.util.security;

import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.InvalidParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.crypto.Cipher;

public class RSAUtil {


	private static final String ALGORITHOM = "RSA";
	// 算法名称
	private static final int KEY_SIZE = 1024;
	//密钥大小
	private static KeyPairGenerator keyPairGen = null;
	private static KeyFactory keyFactory = null;
	/**
	 * 缓存的密钥对。
	 */
	private static KeyPair oneKeyPair = null;

	static {
		try {
			keyPairGen = KeyPairGenerator.getInstance(ALGORITHOM);
			keyFactory = KeyFactory.getInstance(ALGORITHOM);
		} catch (NoSuchAlgorithmException ex) {
			System.out.println(ex.getMessage());
		}
	}
	/**
	 * 生成并返回RSA密钥对。
	 * @return 生成并返回RSA密钥对。
	 */
	private static synchronized KeyPair generateKeyPair() {
		try {
			keyPairGen.initialize(KEY_SIZE, new SecureRandom(new SimpleDateFormat("yyyyMMdd").format(new Date()).getBytes()));
			oneKeyPair = keyPairGen.generateKeyPair();
			return oneKeyPair;
		} catch (InvalidParameterException ex) {
			System.out.println("KeyPairGenerator does not support a key length of " + KEY_SIZE + ".");
			ex.printStackTrace();
		} catch (NullPointerException ex) {
			System.out.println("RSAUtils#KEY_PAIR_GEN is null, can not generate KeyPairGenerator instance.");
			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * 返回RSA密钥对。
	 *
	 * @return 返回RSA密钥对。
	 */
	public static KeyPair getKeyPair() {
		if (oneKeyPair != null) {
			return oneKeyPair;
		}
		return generateKeyPair();
	}

	/**
	 * 根据给定的系数和专用指数构造一个RSA专用的公钥对象。
	 *
	 * @param modulus        系数。
	 * @param publicExponent 专用指数。
	 * @return RSA专用公钥对象。
	 */
	public static RSAPublicKey generateRSAPublicKey(byte[] modulus, byte[] publicExponent) {
		RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(new BigInteger(modulus),
				new BigInteger(publicExponent));
		try {
			return (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);
		} catch (InvalidKeySpecException ex) {
			System.out.println("RSAPublicKeySpec is unavailable.");
			ex.printStackTrace();
		} catch (NullPointerException ex) {
			System.out.println("RSAUtils#KEY_FACTORY is null, can not generate KeyFactory instance.");
			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * 根据给定的系数和专用指数构造一个RSA专用的私钥对象。
	 *
	 * @param modulus         系数。
	 * @param privateExponent 专用指数。
	 * @return RSA专用私钥对象。
	 */
	public static RSAPrivateKey generateRSAPrivateKey(byte[] modulus, byte[] privateExponent) {
		RSAPrivateKeySpec privateKeySpec = new RSAPrivateKeySpec(new BigInteger(modulus),
				new BigInteger(privateExponent));
		try {
			return (RSAPrivateKey) keyFactory.generatePrivate(privateKeySpec);
		} catch (InvalidKeySpecException ex) {
			System.out.println("RSAPrivateKeySpec is unavailable.");
			ex.printStackTrace();
		} catch (NullPointerException ex) {
			System.out.println("RSAUtils#KEY_FACTORY is null, can not generate KeyFactory instance.");
			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * 根据给定的16进制系数和专用指数字符串构造一个RSA专用的私钥对象。
	 *
	 * @param hexModulus         系数。
	 * @param hexPrivateExponent 专用指数。
	 * @return RSA专用私钥对象。
	 */
	public static RSAPrivateKey getRSAPrivateKey(String hexModulus, String hexPrivateExponent) {
		if (isBlank(hexModulus) || isBlank(hexPrivateExponent)) {
			System.out.println("hexModulus and hexPrivateExponent cannot be empty. RSAPrivateKey value is null to return.");
			return null;
		}
		byte[] modulus = null;
		byte[] privateExponent = null;
		try {
			modulus = HexUtil.hex2Bytes(hexModulus);
			privateExponent = HexUtil.hex2Bytes(hexPrivateExponent);
		} catch (Exception ex) {
			System.out.println("hexModulus or hexPrivateExponent value is invalid. return null(RSAPrivateKey).");
			ex.printStackTrace();
		}
		if (modulus != null && privateExponent != null) {
			return generateRSAPrivateKey(modulus, privateExponent);
		}
		return null;
	}

	/**
	 * 根据给定的16进制系数和专用指数字符串构造一个RSA专用的公钥对象。
	 *
	 * @param hexModulus        系数。
	 * @param hexPublicExponent 专用指数。
	 * @return RSA专用公钥对象。
	 */
	public static RSAPublicKey getRSAPublicKey(String hexModulus, String hexPublicExponent) {
		if (isBlank(hexModulus) || isBlank(hexPublicExponent)) {
			System.out.println("hexModulus and hexPublicExponent cannot be empty. return null(RSAPublicKey).");
			return null;
		}
		byte[] modulus = null;
		byte[] publicExponent = null;
		try {
			modulus = HexUtil.hex2Bytes(hexModulus);
			publicExponent = HexUtil.hex2Bytes(hexPublicExponent);
		} catch (Exception ex) {
			System.out.println("hexModulus or hexPublicExponent value is invalid. return null(RSAPublicKey).");
			ex.printStackTrace();
		}
		if (modulus != null && publicExponent != null) {
			return generateRSAPublicKey(modulus, publicExponent);
		}
		return null;
	}

	/**
	 * 使用指定的公钥加密数据。
	 *
	 * @param publicKey 给定的公钥。
	 * @param data      要加密的数据。
	 * @return 加密后的数据。
	 */

	public static byte[] encrypt(PublicKey publicKey, byte[] data) throws Exception {
		Cipher ci = Cipher.getInstance(ALGORITHOM);
		ci.init(Cipher.ENCRYPT_MODE, publicKey);
		return ci.doFinal(data);
	}


	/**
	 * 使用指定的私钥解密数据。
	 *
	 * @param privateKey 给定的私钥。
	 * @param data       要解密的数据。
	 * @return 原数据。
	 */
	public static byte[] decrypt(PrivateKey privateKey, byte[] data) throws Exception {
		Cipher ci = Cipher.getInstance(ALGORITHOM);
		ci.init(Cipher.DECRYPT_MODE, privateKey);
		return ci.doFinal(data);
	}

	/**
	 * 使用给定的公钥加密给定的字符串。
	 *
	 * @param publicKey 给定的公钥。
	 * @param plaintext 字符串。
	 * @return 给定字符串的密文。
	 */
	public static String encryptString(PublicKey publicKey, String plaintext) {
		if (publicKey == null || plaintext == null) {
			return null;
		}
		byte[] data = plaintext.getBytes();
		try {
			byte[] en_data = encrypt(publicKey, data);
			return new String(HexUtil.bytes2Hex(en_data));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * 使用默认的公钥加密给定的字符串。
	 *
	 * @param plaintext 字符串
	 * @return 给定字符串的密文
	 */
	public static String encryptString(String plaintext) {
		if (plaintext == null) {
			return null;
		}
		byte[] data = plaintext.getBytes();
		KeyPair keyPair = getKeyPair();
		try {
			byte[] en_data = encrypt((RSAPublicKey) keyPair.getPublic(), data);
			return new String(HexUtil.bytes2Hex(en_data));
		} catch (NullPointerException ex) {
			System.out.println("keyPair cannot be null.");
			ex.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * 生成由JS的RSA加密的字符串。
	 *
	 * @param publicKey 公钥
	 * @param plaintext 原文字符串
	 * @return 加密后的字符串
	 */
	public static String encryptStringByJs(PublicKey publicKey, String plaintext) {
		if (plaintext == null) {
			return null;
		}
		String text = encryptString(publicKey, reverse(plaintext));

		return text;
	}

	/**
	 * 用默认公钥生成由JS的RSA加密的字符串。
	 * @param plaintext 原文字符串
	 * @return 加密后的字符串
	 */
	public static String encryptStringByJs(String plaintext) {
		if (plaintext == null) {
			return null;
		}
		String text = encryptString(reverse(plaintext));

		return text;
	}

	/**
	 * 使用给定的私钥解密给定的字符串。
	 *
	 * 若私钥为 {@code null}，或者 {@code encrypttext} 为 {@code null}或空字符串则返回 {@code null}。
	 * 私钥不匹配时，返回 {@code null}。
	 *
	 * @param privateKey  给定的私钥。
	 * @param encrypttext 密文。
	 * @return 原文字符串。
	 */
	public static String decryptString(PrivateKey privateKey, String encrypttext) {
		if (privateKey == null || isBlank(encrypttext)) {
			return null;
		}
		try {
			byte[] en_data = HexUtil.hex2Bytes(encrypttext);
			byte[] data = decrypt(privateKey, en_data);
			return new String(data);
		} catch (Exception ex) {
			System.out.println(String.format("\"%s\" Decryption failed. Cause: %s", encrypttext, ex.getCause().getMessage()));

		}
		return null;
	}

	/**
	 * 使用默认的私钥解密给定的字符串。
	 *
	 * @param encrypttext 密文。
	 * @return 原文字符串。
	 */
	public static String decryptString(String encrypttext) {
		if (isBlank(encrypttext)) {
			return null;
		}
		KeyPair keyPair = getKeyPair();
		try {
			byte[] en_data = HexUtil.hex2Bytes(encrypttext);
			byte[] data = decrypt((RSAPrivateKey) keyPair.getPrivate(), en_data);
			return new String(data);
		} catch (NullPointerException ex) {
			System.out.println("keyPair cannot be null.");
			ex.printStackTrace();
		} catch (Exception ex) {
			System.out.println(String.format("\"%s\" Decryption failed. Cause: %s", encrypttext, ex.getMessage()));
			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * 使用指定的私钥解密由JS加密的字符串。
	 *
	 * @param privateKey  私钥
	 * @param encrypttext 密文
	 * @return {@code encrypttext} 的原文字符串
	 */
	public static String decryptStringByJs(PrivateKey privateKey, String encrypttext) {
		String text = decryptString(privateKey, encrypttext);
		if (text == null) {
			return null;
		}
		return reverse(text);
	}

	public static String decryptStringByJs(String encrypttext) {
		String text = decryptString(encrypttext);
		if (text == null) {
			return null;
		}
		return reverse(text);
	}


	/**
	 * 返回已初始化的默认的公钥。
	 * @return 返回已初始化的默认的公钥。
	 */
	public static RSAPublicKey getDefaultPublicKey() {
		KeyPair keyPair = getKeyPair();
		if (keyPair != null) {
			return (RSAPublicKey) keyPair.getPublic();
		}
		return null;
	}

	/**
	 * 返回已初始化的默认的私钥。
	 * @return 返回已初始化的默认的私钥。
	 */
	public static RSAPrivateKey getDefaultPrivateKey() {
		KeyPair keyPair = getKeyPair();
		if (keyPair != null) {
			return (RSAPrivateKey) keyPair.getPrivate();
		}
		return null;
	}

	/**
	 * 逆转字符串
	 * @param str 待逆转的字符串
	 * @return 逆转后字符串
	 */
	private static String reverse(final String str) {
		if (str == null) {
			return null;
		}
		return new StringBuilder(str).reverse().toString();
	}

	/**
	 * 判断非空字符串
	 * @param cs 待判断的CharSequence序列
	 * @return 是否非空
	 */
	private static boolean isBlank(final CharSequence cs) {
		int strLen;
		if (cs == null || (strLen = cs.length()) == 0) {
			return true;
		}
		for (int i = 0; i < strLen; i++) {
			if (Character.isWhitespace(cs.charAt(i)) == false) {
				return false;
			}
		}
		return true;
	}

	private static String publicexponent="010001";
	private static String publicModulus="00aaddf90bdd1f2aedb8a16794757ad8ff7fc477d5da9b65c1e85d3a9d41b6dfa3f8e4bbf6a4b190cdc80ed96ef2d8ab84fa8b00ff6931d744b3514d7061cf009962dacd920ae927de9c55a86fde8717bd14d8020cd69efe5a85179ea008d653df0126d201ff30deda0de270264cf5c6dcd11ebfeb578dd547c3d5a26106befa3d";
	private static String privateexponent = "7ba9fbbda2641465c8e2c85ca24a2f137c1d6dbdd161f6dbc898f0fb2fbc734ff1ab5a7ebfe3e3b18bc1738ce125ffbd56b7941946c153d3dd1452bfab8a6e1b6335c1096d3adc2ea2e830908108386ec37db161299c333d7ac9051aa8095d8348019523e5d8dccac00f8db9447f7d3fc0be14560ea4843d00d34b09db0c64e1";
	private static String privateModulus = "00aaddf90bdd1f2aedb8a16794757ad8ff7fc477d5da9b65c1e85d3a9d41b6dfa3f8e4bbf6a4b190cdc80ed96ef2d8ab84fa8b00ff6931d744b3514d7061cf009962dacd920ae927de9c55a86fde8717bd14d8020cd69efe5a85179ea008d653df0126d201ff30deda0de270264cf5c6dcd11ebfeb578dd547c3d5a26106befa3d";


	public static void main(String[] args) {
		String source = "luoxiaohui";
		PublicKey publicKey = RSAUtil.getRSAPublicKey(publicModulus, publicexponent);
		String encript = RSAUtil.encryptString(publicKey, source);
		System.out.println("加密后数据："+encript);

		PrivateKey privateKey = RSAUtil.getRSAPrivateKey(privateModulus, privateexponent);
		String newSource = RSAUtil.decryptString(privateKey, encript);
		System.out.println("解密后数据:"+newSource);
	}

}
