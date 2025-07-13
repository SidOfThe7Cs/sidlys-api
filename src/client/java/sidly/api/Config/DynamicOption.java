package sidly.api.Config;

import dev.isxander.yacl3.api.Option;

import java.util.Collections;
import java.util.List;
import java.util.function.IntFunction;

public class DynamicOption {
    private final IntFunction<List<Option<?>>> getterFunc;
    private final List<?> options; // the actual data that is saved in config
    private final Runnable cleanFunc;

    public DynamicOption(IntFunction<List<Option<?>>> getterFunc, List<?> options, Runnable cleanFunc) {
        this.getterFunc = getterFunc;
        this.options = options;
        this.cleanFunc = cleanFunc;
    }

    // returns the group as a list of options
    public List<Option<?>> getOptions(int index) {
        if (options != null && index >= 0 && index < options.size()) {
            return getterFunc.apply(index);
        } else return Collections.emptyList();
    }

    public void clean() {
        cleanFunc.run();
    }

    public int getSize(){
        return options.size();
    }
}
