package org.beginFun.visitor;

import javax.xml.crypto.dsig.keyinfo.KeyValue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Variables {
    Map<String, Object> globalVariables;
    ArrayList<Map<String, Object>> variables;
    int currentMap = 0;

    public Variables() {
        this.globalVariables = new HashMap<>();
        this.variables = new ArrayList<Map<String, Object>>();
    }

    public Variables(Map<String, Object> globalVariables, Map<String, Object> variables) {
        this.globalVariables = globalVariables;
        this.variables = new ArrayList<Map<String, Object>>();
        int i = 0;
        this.variables.add(new HashMap<String, Object>());
        for(Map.Entry<String, Object> variable : variables.entrySet()){
            this.variables.get(i).put(variable.getKey(), variable.getValue());
        }
        this.currentMap = 1;
    }

    public Variables(Map<String, Object> globalVariables, ArrayList<Map<String, Object>> variables) {
        this.globalVariables = globalVariables;
        this.variables = new ArrayList<Map<String, Object>>();
        int i = 0;
        for(Map<String, Object> varMap : variables){
            this.variables.add(new HashMap<String, Object>());
            for(Map.Entry<String, Object> variable : varMap.entrySet()){
                this.variables.get(i).put(variable.getKey(), variable.getValue());
            }
        }
        this.currentMap = variables.size();
    }

    public Object findVariable(String varName) {
        Object var;
        if ((var = globalVariables.get(varName)) != null) {
            return var;
        }
        for (Map<String, Object> map : variables) {
            if ((var = map.get(varName)) != null) {
                return var;
            }
        }
        return null;
    }

    public Object addVariable(String name, Object var) {
        if (globalVariables.containsKey(name)) {
            return globalVariables.put(name, var);
        }

        if (currentMap > 0) {
            for (Map<String, Object> map : variables) {
                if (map.containsKey(name)) {
                    return map.put(name, var);
                }
            }
            return variables.get(currentMap - 1).put(name, var);
        }
        return globalVariables.put(name, var);
    }

    public void addBlock() {
        variables.add(new HashMap<String, Object>());
        currentMap++;
    }

    public void removeBlock() {
        if (currentMap <= 0) {
            return;
        }
        variables.remove(currentMap - 1);
        currentMap--;
    }
}
