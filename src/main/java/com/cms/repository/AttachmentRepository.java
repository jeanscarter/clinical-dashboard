package com.cms.repository;

import com.cms.core.Repository;
import com.cms.domain.Attachment;

import java.util.List;

public interface AttachmentRepository extends Repository<Attachment, Integer> {

    List<Attachment> findByClinicalHistoryId(Integer clinicalHistoryId);

    List<Attachment> findByTipo(String tipo);

    void deleteByClinicalHistoryId(Integer clinicalHistoryId);

    long countByClinicalHistoryId(Integer clinicalHistoryId);

    long count();
}
