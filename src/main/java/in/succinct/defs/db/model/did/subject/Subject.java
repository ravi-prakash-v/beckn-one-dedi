package in.succinct.defs.db.model.did.subject;

import com.venky.core.util.Bucket;
import com.venky.swf.db.annotations.column.IS_NULLABLE;
import com.venky.swf.db.annotations.column.relationship.CONNECTED_VIA;
import com.venky.swf.db.annotations.column.ui.HIDDEN;
import com.venky.swf.db.model.Model;
import in.succinct.defs.db.model.did.documents.Document;
import in.succinct.defs.db.model.did.identifier.Did;

import java.util.List;

public interface Subject extends Model , Did {
    
    // If  null then subject is a controller or the specified controller controls this subject's information
    @IS_NULLABLE(true)
    Long getControllerId();
    void setControllerId(Long id);
    Subject getController();
    
    
    List<Document> getDocuments(); //Owns several documents. Some self signed, and some by others.
    
    List<Service> getServices();
    
    List<VerificationMethod> getVerificationMethods();
    
    @CONNECTED_VIA("CONTROLLER_ID")
    @HIDDEN
    List<Subject> getControlledSubjects();
    
    public Bucket getModCount();
    public void setModCount(Bucket modCount);
    
    
}
