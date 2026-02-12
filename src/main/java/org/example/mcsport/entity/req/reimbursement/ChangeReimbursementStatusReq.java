package org.example.mcsport.entity.req.reimbursement;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeReimbursementStatusReq {
    private Long reimbursement_id;
    private String status;
    private String review_comment;
}
