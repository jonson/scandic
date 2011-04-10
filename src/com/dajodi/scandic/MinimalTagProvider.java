package com.dajodi.scandic;

import org.htmlcleaner.ITagInfoProvider;
import org.htmlcleaner.TagInfo;

import java.util.HashMap;

/**
 * This class is automatically created from ConfigFileTagProvider which reads
 * default XML configuration file with tag descriptions.
 * It is used as default tag info provider.
 * Class is created for performance purposes - parsing XML file requires some
 * processing time.
 */
public class MinimalTagProvider extends HashMap<String, TagInfo> implements ITagInfoProvider {

    protected static final int HEAD_AND_BODY = 0;
	protected static final int HEAD = 1;
	protected static final int BODY = 2;

	protected static final int CONTENT_ALL = 0;
	protected static final int CONTENT_NONE = 1;
	protected static final int CONTENT_TEXT = 2;

    // singleton instance, used if no other TagInfoProvider is specified
    private static MinimalTagProvider _instance;

    /**
     * @return Singleton instance of this class.
     */
    public static synchronized MinimalTagProvider getInstance() {
        if (_instance == null) {
            _instance = new MinimalTagProvider();
        }
        return _instance;
    }

    public MinimalTagProvider() {
        TagInfo tagInfo;

        tagInfo = new TagInfo("div", CONTENT_ALL, BODY, false, false, false);
        tagInfo.defineCloseBeforeCopyInsideTags("a,bdo,strong,em,q,b,i,u,tt,sub,sup,big,small,strike,s,font");
        tagInfo.defineCloseBeforeTags("p,address,label,abbr,acronym,dfn,kbd,samp,var,cite,code,param,xml");
        this.put("div", tagInfo);

        tagInfo = new TagInfo("span", CONTENT_ALL, BODY, false, false, false);
        this.put("span", tagInfo);

        /**
        tagInfo = new TagInfo("h1", CONTENT_ALL, BODY, false, false, false);
        tagInfo.defineCloseBeforeCopyInsideTags("a,bdo,strong,em,q,b,i,u,tt,sub,sup,big,small,strike,s,font");
        tagInfo.defineCloseBeforeTags("h1,h2,h3,h4,h5,h6,p,address,label,abbr,acronym,dfn,kbd,samp,var,cite,code,param,xml");
        this.put("h1", tagInfo);

        tagInfo = new TagInfo("h2", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseBeforeCopyInsideTags("a,bdo,strong,em,q,b,i,u,tt,sub,sup,big,small,strike,s,font");
        tagInfo.defineCloseBeforeTags("h1,h2,h3,h4,h5,h6,p,address,label,abbr,acronym,dfn,kbd,samp,var,cite,code,param,xml");
        this.put("h2", tagInfo);

        tagInfo = new TagInfo("h3", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseBeforeCopyInsideTags("a,bdo,strong,em,q,b,i,u,tt,sub,sup,big,small,strike,s,font");
        tagInfo.defineCloseBeforeTags("h1,h2,h3,h4,h5,h6,p,address,label,abbr,acronym,dfn,kbd,samp,var,cite,code,param,xml");
        this.put("h3", tagInfo);

        tagInfo = new TagInfo("h4", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseBeforeCopyInsideTags("a,bdo,strong,em,q,b,i,u,tt,sub,sup,big,small,strike,s,font");
        tagInfo.defineCloseBeforeTags("h1,h2,h3,h4,h5,h6,p,address,label,abbr,acronym,dfn,kbd,samp,var,cite,code,param,xml");
        this.put("h4", tagInfo);

        tagInfo = new TagInfo("h5", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseBeforeCopyInsideTags("a,bdo,strong,em,q,b,i,u,tt,sub,sup,big,small,strike,s,font");
        tagInfo.defineCloseBeforeTags("h1,h2,h3,h4,h5,h6,p,address,label,abbr,acronym,dfn,kbd,samp,var,cite,code,param,xml");
        this.put("h5", tagInfo);

        tagInfo = new TagInfo("h6", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseBeforeCopyInsideTags("a,bdo,strong,em,q,b,i,u,tt,sub,sup,big,small,strike,s,font");
        tagInfo.defineCloseBeforeTags("h1,h2,h3,h4,h5,h6,p,address,label,abbr,acronym,dfn,kbd,samp,var,cite,code,param,xml");
        this.put("h6", tagInfo);
         **/

        tagInfo = new TagInfo("p", CONTENT_ALL, BODY, false, false, false);
        tagInfo.defineCloseBeforeCopyInsideTags("a,bdo,strong,em,q,b,i,u,tt,sub,sup,big,small,strike,s,font");
        tagInfo.defineCloseBeforeTags("p,p,address,label,abbr,acronym,dfn,kbd,samp,var,cite,code,param,xml");
        this.put("p", tagInfo);

        tagInfo = new TagInfo("a", CONTENT_ALL, BODY, false, false, false);
        tagInfo.defineCloseBeforeTags("a");
        this.put("a", tagInfo);

        tagInfo = new TagInfo("table", CONTENT_ALL, BODY, false, false, false);
        tagInfo.defineAllowedChildrenTags("tr,tbody,thead,tfoot,colgroup,col,form,caption,tr");
        tagInfo.defineCloseBeforeCopyInsideTags("a,bdo,strong,em,q,b,i,u,tt,sub,sup,big,small,strike,s,font");
        tagInfo.defineCloseBeforeTags("tr,thead,tbody,tfoot,caption,colgroup,table,address,label,abbr,acronym,dfn,kbd,samp,var,cite,code,param");
        this.put("table", tagInfo);

        tagInfo = new TagInfo("tr", CONTENT_ALL, BODY, false, false, false);
        tagInfo.defineFatalTags("table");
        tagInfo.defineRequiredEnclosingTags("tbody");
        tagInfo.defineAllowedChildrenTags("td,th");
        tagInfo.defineHigherLevelTags("thead,tfoot");
        tagInfo.defineCloseBeforeTags("tr,td,th,caption,colgroup");
        this.put("tr", tagInfo);

        tagInfo = new TagInfo("td", CONTENT_ALL, BODY, false, false, false);
        tagInfo.defineFatalTags("table");
        tagInfo.defineRequiredEnclosingTags("tr");
        tagInfo.defineCloseBeforeTags("td,th,caption,colgroup");
        this.put("td", tagInfo);

        tagInfo = new TagInfo("th", CONTENT_ALL, BODY, false, false, false);
        tagInfo.defineFatalTags("table");
        tagInfo.defineRequiredEnclosingTags("tr");
        tagInfo.defineCloseBeforeTags("td,th,caption,colgroup");
        this.put("th", tagInfo);

        tagInfo = new TagInfo("tbody", CONTENT_ALL, BODY, false, false, false);
        tagInfo.defineFatalTags("table");
        tagInfo.defineAllowedChildrenTags("tr,form");
        tagInfo.defineCloseBeforeTags("td,th,tr,tbody,thead,tfoot,caption,colgroup");
        this.put("tbody", tagInfo);

        tagInfo = new TagInfo("thead", CONTENT_ALL, BODY, false, false, false);
        tagInfo.defineFatalTags("table");
        tagInfo.defineAllowedChildrenTags("tr,form");
        tagInfo.defineCloseBeforeTags("td,th,tr,tbody,thead,tfoot,caption,colgroup");
        this.put("thead", tagInfo);

        tagInfo = new TagInfo("tfoot", CONTENT_ALL, BODY, false, false, false);
        tagInfo.defineFatalTags("table");
        tagInfo.defineAllowedChildrenTags("tr,form");
        tagInfo.defineCloseBeforeTags("td,th,tr,tbody,thead,tfoot,caption,colgroup");
        this.put("tfoot", tagInfo);

        tagInfo = new TagInfo("col", CONTENT_NONE, BODY, false, false, false);
        tagInfo.defineFatalTags("table");
        this.put("col", tagInfo);

        tagInfo = new TagInfo("colgroup", CONTENT_ALL, BODY, false, false, false);
        tagInfo.defineFatalTags("table");
        tagInfo.defineAllowedChildrenTags("col");
        tagInfo.defineCloseBeforeTags("td,th,tr,tbody,thead,tfoot,caption,colgroup");
        this.put("colgroup", tagInfo);

        tagInfo = new TagInfo("caption", CONTENT_ALL, BODY, false, false, false);
        tagInfo.defineFatalTags("table");
        tagInfo.defineCloseBeforeTags("td,th,tr,tbody,thead,tfoot,caption,colgroup");
        this.put("caption", tagInfo);

        tagInfo = new TagInfo("form", CONTENT_ALL, BODY, false, false, true);
        tagInfo.defineForbiddenTags("form");
        tagInfo.defineCloseBeforeCopyInsideTags("a,bdo,strong,em,q,b,i,u,tt,sub,sup,big,small,strike,s,font");
        tagInfo.defineCloseBeforeTags("option,optgroup,textarea,select,fieldset,p,address,label,abbr,acronym,dfn,kbd,samp,var,cite,code,param,xml");
        this.put("form", tagInfo);

        tagInfo = new TagInfo("input", CONTENT_NONE, BODY, false, false, false);
        tagInfo.defineCloseBeforeTags("select,optgroup,option");
        this.put("input", tagInfo);

        tagInfo = new TagInfo("fieldset", CONTENT_ALL, BODY, false, false, false);
        tagInfo.defineCloseBeforeCopyInsideTags("a,bdo,strong,em,q,b,i,u,tt,sub,sup,big,small,strike,s,font");
        tagInfo.defineCloseBeforeTags("p,address,label,abbr,acronym,dfn,kbd,samp,var,cite,code,param,xml");
        this.put("fieldset", tagInfo);

        tagInfo = new TagInfo("b", CONTENT_ALL, BODY, false, false, false);
        tagInfo.defineCloseInsideCopyAfterTags("u,i,tt,sub,sup,big,small,strike,blink,s");
        this.put("b", tagInfo);

        tagInfo = new TagInfo("i", CONTENT_ALL, BODY, false, false, false);
        tagInfo.defineCloseInsideCopyAfterTags("b,u,tt,sub,sup,big,small,strike,blink,s");
        this.put("i", tagInfo);

        tagInfo = new TagInfo("u", CONTENT_ALL, BODY, true, false, false);
        tagInfo.defineCloseInsideCopyAfterTags("b,i,tt,sub,sup,big,small,strike,blink,s");
        this.put("u", tagInfo);

        
    }

    public TagInfo getTagInfo(String tagName) {
        return get(tagName);
    }

    /**
     * Removes tag info with specified name.
     * @param tagName Name of the tag to be removed from the tag provider.
     */
    public void removeTagInfo(String tagName) {
        if (tagName != null) {
            remove(tagName.toLowerCase());
        }
    }

    /**
     * Sets new tag info.
     * @param tagInfo tag info to be added to the provider.
     */
    public void addTagInfo(TagInfo tagInfo) {
        if (tagInfo != null) {
            put(tagInfo.getName().toLowerCase(), tagInfo);
        }
    }

}