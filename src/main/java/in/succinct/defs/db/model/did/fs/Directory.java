package in.succinct.defs.db.model.did.fs;

import com.venky.swf.db.model.Model;
import in.succinct.defs.db.model.did.documents.Document;

public interface Directory extends Model {
    String getName();
    void setName(String name);
    
    Long getSchemaId();
    void setSchemaId(Long id);
    Document getSchema();
}
