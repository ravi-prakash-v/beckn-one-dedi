package in.succinct.defs.db.model.did.documents;

import com.venky.swf.db.annotations.column.IS_NULLABLE;
import com.venky.swf.db.annotations.column.UNIQUE_KEY;
import com.venky.swf.db.model.Model;
import in.succinct.defs.db.model.did.identifier.Did;
import in.succinct.defs.db.model.did.subject.VerificationMethod;

public interface Attestation extends Model , Did {
    @IS_NULLABLE(false)
    @UNIQUE_KEY("K2")
    Long getDocumentId();
    void setDocumentId(Long id);
    Document getDocument();
    
    
    @IS_NULLABLE(false)
    @UNIQUE_KEY("K2")
    Long getVerificationMethodId();
    void setVerificationMethodId(Long id);
    VerificationMethod getVerificationMethod();
    //Must be of purpose assertion
    
    String getSignature();
    void setSignature(String signature);
    
    
    boolean isVerified();
    void setVerified(boolean verified);
    
}
