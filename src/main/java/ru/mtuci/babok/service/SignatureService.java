package ru.mtuci.babok.service;

import ru.mtuci.babok.model.SignatureEntity;

public interface SignatureService {
    byte[] sign(byte[] data);
    boolean verify(byte[] data, byte[] signatureBytes);
    byte[] getDataToSign(SignatureEntity entity);
}
