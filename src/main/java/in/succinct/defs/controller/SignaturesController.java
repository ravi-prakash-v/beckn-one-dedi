package in.succinct.defs.controller;

import com.venky.swf.path.Path;
import in.succinct.defs.db.model.did.subject.VerificationMethod;

public class SignaturesController extends AbstractDirectoryController<VerificationMethod> {
    public SignaturesController(Path path) {
        super(path);
    }
}
