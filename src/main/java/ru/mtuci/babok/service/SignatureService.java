package ru.mtuci.babok.service;

import ru.mtuci.babok.model.SignatureEntity;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

public interface SignatureService {
    byte[] sign(byte[] data) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException;
    boolean verify(byte[] data, byte[] signatureBytes) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException;
    byte[] getDataToSign(SignatureEntity entity);
}
