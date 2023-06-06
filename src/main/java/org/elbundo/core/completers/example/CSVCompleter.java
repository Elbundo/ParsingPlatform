package org.elbundo.core.completers.example;

import org.elbundo.core.completers.Completer;
import org.elbundo.core.parsers.Result;
import org.elbundo.model.Product;

import java.lang.reflect.Field;

public class CSVCompleter implements Completer {

    public CSVCompleter(String path) {
        System.out.println("CreateFile");
        for(Field field : Product.class.getDeclaredFields()) {
            System.out.print(field.getName() + " ");
        }
        System.out.println();
    }

    @Override
    public void complete(Result result) {
        Product res = (Product) result;
        for(Field field : res.getClass().getDeclaredFields()) {
            try {
                System.out.print(field.get(result).toString());
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println();
    }
}
