package in.succinct.defs.controller;

import com.venky.core.security.Crypt;
import com.venky.core.string.StringUtil;
import com.venky.core.util.ObjectUtil;
import com.venky.swf.db.model.Model;
import com.venky.swf.db.model.application.ApplicationUtil;
import com.venky.swf.exceptions.AccessDeniedException;
import com.venky.swf.path.Path;
import in.succinct.defs.db.model.did.documents.Document;
import in.succinct.defs.db.model.did.documents.Signature;
import in.succinct.defs.db.model.did.subject.Service;
import in.succinct.defs.db.model.did.subject.Subject;
import in.succinct.defs.db.model.did.subject.VerificationMethod;
import in.succinct.defs.db.model.did.subject.VerificationMethod.Purpose;
import in.succinct.defs.util.SignatureChallenge;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class SubjectsController extends AbstractDirectoryController<Subject> {
    
    public SubjectsController(Path path) {
        super(path);
    }
    
    
    

    @Override
    protected Map<Class<? extends Model>, List<String>> getIncludedModelFields() {
        Map<Class<? extends Model>, List<String>> map =  super.getIncludedModelFields();
        if (getReturnIntegrationAdaptor() == null) {
            return map;
        }
        addToIncludedModelFieldsMap(map,Subject.class,List.of("MOD_COUNT" , "NAME"));
        addToIncludedModelFieldsMap(map, Signature.class, List.of("DOCUMENT_ID"  , "NAME" ));
        addToIncludedModelFieldsMap(map, Service.class, List.of("SUBJECT_ID" , "NAME"));
        addToIncludedModelFieldsMap(map, Document.class, List.of("SUBJECT_ID" ,"NAME" ,"STREAM","STREAM_CONTENT_NAME"));
        addToIncludedModelFieldsMap(map, VerificationMethod.class, List.of("CONTROLLER_ID"  , "NAME" ,"CHALLENGE" ));
        
        return map;
        
    }
    

}
