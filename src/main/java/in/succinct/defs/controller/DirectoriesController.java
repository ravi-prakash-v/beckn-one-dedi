package in.succinct.defs.controller;

import com.venky.core.string.StringUtil;
import com.venky.swf.db.annotations.column.ui.mimes.MimeType;
import com.venky.swf.db.model.Model;
import com.venky.swf.db.model.reflection.ModelReflector;
import com.venky.swf.path.Path;
import com.venky.swf.views.BytesView;
import com.venky.swf.views.View;
import in.succinct.defs.db.model.did.documents.Document;
import in.succinct.defs.db.model.did.fs.Directory;
import in.succinct.defs.db.model.did.fs.Record;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class DirectoriesController extends AbstractDirectoryController<Directory>{
    public DirectoriesController(Path path) {
        super(path);
    }
    
    @Override
    protected String did(String name) {
        return String.format("%s",
                getPath().getTarget().replaceAll("/schema$","")); // Target includes name
        
    }
    
    @Override
    protected View respond(List<Directory> models) {
        if (!getPath().getTarget().endsWith("/schema")){
            return super.respond(models);
        }else if (models.size() > 1){
            throw new RuntimeException("Incomplete Did ");
        }else if (models.isEmpty()){
            throw new RuntimeException("Invalid directory");
        }
        Directory directory = models.get(0);
        Document document = directory.getSchema();
        String schema = "{}";
        MimeType mimeType = MimeType.APPLICATION_JSON;
        if (document != null ){
            schema = StringUtil.read(document.getStream());
            mimeType = MimeType.getMimeType(document.getStreamContentType());
        }
        return new BytesView(getPath(),schema.getBytes(StandardCharsets.UTF_8),mimeType);
    }
    
    @Override
    protected Map<Class<? extends Model>, List<String>> getIncludedModelFields() {
        Map<Class<? extends Model>, List<String>> map = super.getIncludedModelFields();
        if (getReturnIntegrationAdaptor() == null) {
            return map;
        }
        
        List<String> excluded = ModelReflector.instance(Directory.class).getFields();
        excluded.remove("DID");
        excluded.remove("SCHEMA_ID");
        addToIncludedModelFieldsMap(map,Directory.class,excluded);
        
        addToIncludedModelFieldsMap(map, Document.class, List.of("SUBJECT_ID" ,"NAME" ));
        
        
        excluded = ModelReflector.instance(Record.class).getFields();
        excluded.remove("DID");
        excluded.remove("DOCUMENT_ID");
        addToIncludedModelFieldsMap(map,Record.class,excluded);
        
        
        
        return map;
    }
    
    
}
