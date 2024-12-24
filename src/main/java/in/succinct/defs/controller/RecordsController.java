package in.succinct.defs.controller;

import com.venky.swf.db.model.Model;
import com.venky.swf.db.model.reflection.ModelReflector;
import com.venky.swf.path.Path;
import in.succinct.defs.db.model.did.documents.Document;
import in.succinct.defs.db.model.did.fs.Directory;
import in.succinct.defs.db.model.did.fs.Record;

import java.util.List;
import java.util.Map;

public class RecordsController extends AbstractDirectoryController<Record>{
    public RecordsController(Path path) {
        super(path);
    }
    @Override
    protected Map<Class<? extends Model>, List<String>> getIncludedModelFields() {
        Map<Class<? extends Model>, List<String>> map = super.getIncludedModelFields();
        if (getReturnIntegrationAdaptor() == null) {
            return map;
        }
        
        List<String> excluded = ModelReflector.instance(Record.class).getFields();
        excluded.remove("NAME");
        excluded.remove("DID");
        excluded.remove("DOCUMENT_ID");
        
        addToIncludedModelFieldsMap(map,Record.class,excluded);
        
        excluded = ModelReflector.instance(Document.class).getFields();
        excluded.remove("DID");
        addToIncludedModelFieldsMap(map, Document.class, excluded);
        
        
        return map;
    }
    
    
}
