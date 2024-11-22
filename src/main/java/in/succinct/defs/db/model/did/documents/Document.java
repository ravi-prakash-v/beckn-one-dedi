package in.succinct.defs.db.model.did.documents;
import com.venky.swf.db.annotations.column.ui.HIDDEN;
import com.venky.swf.db.annotations.column.ui.PROTECTION;
import com.venky.swf.db.annotations.column.ui.PROTECTION.Kind;
import com.venky.swf.db.model.Model;
import in.succinct.defs.db.model.did.identifier.Did;
import in.succinct.defs.db.model.did.subject.Subject;

import java.io.InputStream;
import java.util.List;
public interface Document extends Did, Model {
    
    Long getSubjectId();
    void setSubjectId(Long id);
    Subject getSubject();
    
    public InputStream getStream();
    public void setStream(InputStream is);
    
    @PROTECTION(Kind.NON_EDITABLE)
    public String getStreamContentName();
    public void setStreamContentName(String name);
    
    @HIDDEN
    @PROTECTION(Kind.NON_EDITABLE)
    public String getStreamContentType();
    public void setStreamContentType(String contentType);
    @HIDDEN
    @PROTECTION(Kind.NON_EDITABLE)
    public int getStreamContentSize();
    public void setStreamContentSize(int size);
    List<Signature> getSignatures(); //Multiple controllers can sign the document.
    
    
}
