package in.succinct.defs.controller;

import com.venky.swf.path.Path;
import in.succinct.defs.db.model.did.subject.VerificationMethod;

public class DocumentsController extends AbstractDirectoryController<VerificationMethod> {
    public DocumentsController(Path path) {
        super(path);
    }
}
