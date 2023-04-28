package com.element.document;

import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author sjl
 */
public class DocumentProvider extends AbstractDocumentationProvider {

    public static Properties properties = new Properties();
    static {
        try(InputStream resourceAsStream = DocumentProvider.class .getClassLoader().getResourceAsStream("element-tips.properties")) {
            properties.load(resourceAsStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Nullable
    public String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
        if (element instanceof IProperty) {
            return "\"" + renderPropertyValue((IProperty)element) + "\"" + getLocationString(element);
        }
        return null;
    }

    private static String getLocationString(PsiElement element) {
        PsiFile file = element.getContainingFile();
        return file != null ? " [" + file.getName() + "]" : "";
    }

    @NotNull
    private static String renderPropertyValue(IProperty prop) {
        String raw = prop.getValue();
        if (raw == null) {
            return "<i>empty</i>";
        }
        return StringUtil.escapeXml(raw);
    }

    @Override
    public String generateDoc(final PsiElement element, @Nullable final PsiElement originalElement) {
        // 相关处理，不处理返回null
        String text = null;
        if (originalElement != null) {
            text = originalElement.getText();
        }

        if (null != text) {
            String doc = "doc: " + text;
            String textHandle = text.replaceAll("-", "").replaceAll("\n|\r\n", "");
            if(properties.getProperty(textHandle) != null){
                doc = properties.getProperty(textHandle);
            }

            if ("doc: ".equals(doc)) {
                return null;
            }else{
                return doc;
            }
        }
        return null;
    }

}
