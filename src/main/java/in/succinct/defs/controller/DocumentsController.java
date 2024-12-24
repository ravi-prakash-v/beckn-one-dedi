package in.succinct.defs.controller;

import com.venky.core.io.ByteArrayInputStream;
import com.venky.core.string.StringUtil;
import com.venky.swf.db.annotations.column.ui.mimes.MimeType;
import com.venky.swf.db.model.Model;
import com.venky.swf.db.model.reflection.ModelReflector;
import com.venky.swf.path.Path;
import com.venky.swf.views.BytesView;
import com.venky.swf.views.View;
import in.succinct.defs.db.model.did.documents.Document;
import in.succinct.defs.db.model.did.documents.Signature;
import in.succinct.defs.db.model.did.fs.Directory;
import in.succinct.defs.db.model.did.subject.Subject;
import in.succinct.defs.db.model.did.subject.VerificationMethod;

import javax.print.DocFlavor.STRING;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class DocumentsController extends AbstractDirectoryController<Document> {
    public DocumentsController(Path path) {
        super(path);
    }
    
    @Override
    protected String did(String name) {
        return String.format("%s",
                getPath().getTarget().replaceAll("/stream$","")); // Target includes name
        
    }
    
    @Override
    protected View respond(List<Document> models) {
        if (!getPath().getTarget().endsWith("/stream")){
            return super.respond(models);
        }else if (models.size() > 1){
            throw new RuntimeException("Incomplete Did ");
        }else if (models.isEmpty()){
            throw new RuntimeException("Invalid Did");
        }
        Document document = models.get(0);
        
        return new BytesView(getPath(), StringUtil.readBytes(document.getStream()), document.getStreamContentType());
    }
    @Override
    protected Map<Class<? extends Model>, List<String>> getIncludedModelFields() {
        Map<Class<? extends Model>, List<String>> map = super.getIncludedModelFields();
        if (getReturnIntegrationAdaptor() == null) {
            return map;
        }
        List<String> excluded = null;
        
        addToIncludedModelFieldsMap(map, Document.class, List.of("SUBJECT_ID" ,"NAME" ));
        
        excluded = ModelReflector.instance(Signature.class).getFields();
        excluded.removeAll(List.of("SIGNATURE","VERIFIED" , "VERIFICATION_METHOD_ID"));
        addToIncludedModelFieldsMap(map, Signature.class, excluded);
        
        
        excluded = ModelReflector.instance(VerificationMethod.class).getFields();
        excluded.removeAll(List.of("DID","VERIFIED"));
        addToIncludedModelFieldsMap(map, VerificationMethod.class, excluded);
        
        
        return map;
    }
    

}
