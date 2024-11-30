package in.succinct.defs.controller;

import com.venky.swf.db.model.Model;
import com.venky.swf.path.Path;
import com.venky.swf.views.View;
import in.succinct.defs.db.model.did.documents.Document;
import in.succinct.defs.db.model.did.subject.Subject;
import in.succinct.defs.db.model.did.subject.VerificationMethod;

import java.util.List;
import java.util.Map;

public class DocumentsController extends AbstractDirectoryController<Document> {
    public DocumentsController(Path path) {
        super(path);
    }
    
    @Override
    protected Map<Class<? extends Model>, List<String>> getIncludedModelFields() {
        Map<Class<? extends Model>, List<String>> map = super.getIncludedModelFields();
        if (getReturnIntegrationAdaptor() == null) {
            return map;
        }
        addToIncludedModelFieldsMap(map, Document.class, List.of("SUBJECT_ID" ,"NAME" ));
        return map;
    }
    
}
