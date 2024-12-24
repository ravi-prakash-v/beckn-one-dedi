package in.succinct.defs.db.model.did.fs;

import com.venky.swf.db.model.Model;
import in.succinct.defs.db.model.did.documents.Document;
import in.succinct.defs.db.model.did.identifier.Did;

import java.util.List;

public interface Directory extends Model , Did {
    
    Long getSchemaId();
    void setSchemaId(Long id);
    Document getSchema();
    
    
    List<Record> getRecords();
}
