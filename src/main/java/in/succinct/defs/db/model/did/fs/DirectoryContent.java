package in.succinct.defs.db.model.did.fs;

import com.venky.swf.db.model.Model;
import in.succinct.defs.db.model.did.documents.Document;

public interface DirectoryContent extends Model {
    Long getDirectoryId();
    void setDirectoryId(Long id);
    Directory getDirectory();
    
    Long getDocumentId();
    void setDocumentId(Long id);
    Document getDocument();
    
}
