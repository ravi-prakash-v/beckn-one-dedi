package in.succinct.defs.controller;

import com.venky.swf.db.model.Model;
import com.venky.swf.path.Path;
import in.succinct.defs.db.model.did.documents.Document;
import in.succinct.defs.db.model.did.documents.Attestation;
import in.succinct.defs.db.model.did.subject.VerificationMethod;

import java.util.List;
import java.util.Map;

public class AttestationsController extends AbstractDirectoryController<Attestation> {
    public AttestationsController(Path path) {
        super(path);
    }
    
    @Override
    protected Map<Class<? extends Model>, List<String>> getIncludedModelFields() {
        Map<Class<? extends Model>, List<String>> map = super.getIncludedModelFields();
        if (getReturnIntegrationAdaptor() == null) {
            return map;
        }
        addToIncludedModelFieldsMap(map, Document.class, List.of("SUBJECT_ID" ,"NAME" ,"STREAM","STREAM_CONTENT_NAME"));
        addToIncludedModelFieldsMap(map, Attestation.class, List.of( "NAME" ));
        addToIncludedModelFieldsMap(map, VerificationMethod.class, List.of("CONTROLLER_ID"  , "NAME" ,"CHALLENGE" , "HASHING_ALGORITHM","PUBLIC_KEY","PURPOSE","TYPE","VERIFIED"));
        
        return map;
    }
    
}
