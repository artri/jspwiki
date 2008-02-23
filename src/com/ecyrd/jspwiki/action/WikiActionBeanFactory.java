package com.ecyrd.jspwiki.action;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import net.sourceforge.stripes.action.RedirectResolution;

import org.apache.log4j.Logger;

import com.ecyrd.jspwiki.*;
import com.ecyrd.jspwiki.parser.MarkupParser;
import com.ecyrd.jspwiki.providers.ProviderException;
import com.ecyrd.jspwiki.ui.WikiInterceptor;

/**
 * <p>
 * Class that resolves special pages and JSPs on behalf of a WikiEngine.
 * WikiActionBeanResolver will automatically resolve page names with
 * singular/plural variants. It can also detect the correct Command based on
 * parameters supplied in an HTTP request, or due to the JSP being accessed.
 * </p>
 * <p>
 * <p>
 * WikiActionBeanResolver's static {@link #findCommand(String)} method is the
 * simplest method; it looks up and returns the Command matching a supplied wiki
 * context. For example, looking up the request context <code>view</code>
 * returns {@link PageCommand#VIEW}. Use this method to obtain static Command
 * instances that aren't targeted at a particular page or group.
 * </p>
 * <p>
 * For more complex lookups in which the caller supplies an HTTP request,
 * {@link #findCommand(HttpServletRequest, String)} will look up and return the
 * correct Command. The String parameter <code>defaultContext</code> supplies
 * the request context to use if it cannot be detected. However, note that the
 * default wiki context may be over-ridden if the request was for a "special
 * page."
 * </p>
 * <p>
 * For example, suppose the WikiEngine's properties specify a special page
 * called <code>UserPrefs</code> that redirects to
 * <code>UserPreferences.jsp</code>. The ordinary lookup method
 * {@linkplain #findCommand(String)} using a supplied context <code>view</code>
 * would return {@link PageCommand#VIEW}. But the
 * {@linkplain #findCommand(HttpServletRequest, String)} method, when passed the
 * same context (<code>view</code>) and an HTTP request containing the page
 * parameter value <code>UserPrefs</code>, will instead return
 * {@link WikiCommand#PREFS}.
 * </p>
 * 
 * @author Andrew Jaquith
 * @param <T>
 * @since 2.4.22
 */
public final class WikiActionBeanFactory
{
    private static final Logger log = Logger.getLogger(WikiActionBeanFactory.class);
    
    private static final long serialVersionUID = 1L;

    /** Prefix in jspwiki.properties signifying special page keys. */
    private static final String PROP_SPECIALPAGE = "jspwiki.specialPage.";

    /** Private map with JSPs as keys, Resolutions as values */
    private final Map<String, RedirectResolution> m_specialRedirects;

    private final WikiEngine m_engine;

    /** If true, we'll also consider english plurals (+s) a match. */
    private boolean m_matchEnglishPlurals;

    /**
     *  Contains the absolute path of the JSPWiki Web application without the
     *  actual servlet; in other words, the absolute or relative path to
     *  this webapp's root path. If no base URL is specified in
     *  <code>jspwiki.properties</code>, the value will be an empty string.
     */
    private final String m_pathPrefix;

    /**
     * Constructs a WikiActionBeanResolver for a given WikiEngine. This
     * constructor will extract the special page references for this wiki and
     * store them in a cache used for resolution.
     * 
     * @param engine
     *            the wiki engine
     * @param properties
     *            the properties used to initialize the wiki
     */
    public WikiActionBeanFactory(WikiEngine engine, Properties properties)
    {
        super();
        m_engine = engine;
        m_specialRedirects = new HashMap<String, RedirectResolution>();

        // Skim through the properties and look for anything with
        // the "special page" prefix. Create maps that allow us
        // look up the correct ActionBean based on special page name.
        // If a matching command isn't found, create a RedirectCommand.
        for (Map.Entry entry : properties.entrySet())
        {
            String key = (String) entry.getKey();
            if (key.startsWith(PROP_SPECIALPAGE))
            {
                String specialPage = key.substring(PROP_SPECIALPAGE.length());
                String redirectUrl = (String) entry.getValue();
                if (specialPage != null && redirectUrl != null)
                {
                    specialPage = specialPage.trim();
                    redirectUrl = redirectUrl.trim();
                    RedirectResolution resolution = m_specialRedirects.get(specialPage);
                    if (resolution == null)
                    {
                        resolution = new RedirectResolution(redirectUrl);
                        m_specialRedirects.put(specialPage, resolution);
                    }
                }
            }
        }

        // Do we match plurals?
        m_matchEnglishPlurals = TextUtil.getBooleanProperty(properties, WikiEngine.PROP_MATCHPLURALS, true);
        
        // Initialize the path prefix for building URLs
        // NB this was stolen and adapted from the old URLConstructor code...
        String baseurl = engine.getBaseURL();
        String tempPath = "";
        if( baseurl != null && baseurl.length() > 0 )
        {
            try
            {
                URL url = new URL( baseurl );
                tempPath = url.getPath();
            }
            catch( MalformedURLException e )
            {
                tempPath = "/JSPWiki"; // Just a guess.
            }
        }
        m_pathPrefix = tempPath;
    }
    
    /**
     * Returns the path prefix for building URLs. Should be deprecated as we move
     * to Stripes for URL generation.
     * @return
     */
    public String getPathPrefix()
    {
        return m_pathPrefix;
    }

    /**
     * <p>
     * Returns the correct page name, or <code>null</code>, if no such page
     * can be found. Aliases are considered.
     * </p>
     * <p>
     * In some cases, page names can refer to other pages. For example, when you
     * have matchEnglishPlurals set, then a page name "Foobars" will be
     * transformed into "Foobar", should a page "Foobars" not exist, but the
     * page "Foobar" would. This method gives you the correct page name to refer
     * to.
     * </p>
     * <p>
     * This facility can also be used to rewrite any page name, for example, by
     * using aliases. It can also be used to check the existence of any page.
     * </p>
     * 
     * @since 2.4.20
     * @param page
     *            the page name.
     * @return The rewritten page name, or <code>null</code>, if the page
     *         does not exist.
     */
    public final String getFinalPageName(String page) throws ProviderException
    {
        boolean isThere = simplePageExists(page);
        String finalName = page;

        if (!isThere && m_matchEnglishPlurals)
        {
            if (page.endsWith("s"))
            {
                finalName = page.substring(0, page.length() - 1);
            }
            else
            {
                finalName += "s";
            }

            isThere = simplePageExists(finalName);
        }

        if (!isThere)
        {
            finalName = MarkupParser.wikifyLink(page);
            isThere = simplePageExists(finalName);

            if (!isThere && m_matchEnglishPlurals)
            {
                if (finalName.endsWith("s"))
                {
                    finalName = finalName.substring(0, finalName.length() - 1);
                }
                else
                {
                    finalName += "s";
                }

                isThere = simplePageExists(finalName);
            }
        }

        return isThere ? finalName : null;
    }

    /**
     * <p>
     * If the page is a special page, this method returns a direct URL to that
     * page; otherwise, it returns <code>null</code>.
     * </p>
     * <p>
     * Special pages are non-existant references to other pages. For example,
     * you could define a special page reference "RecentChanges" which would
     * always be redirected to "RecentChanges.jsp" instead of trying to find a
     * Wiki page called "RecentChanges".
     * </p>
     * TODO: fix this algorithm
     */
    public final String getSpecialPageReference(String page)
    {
        RedirectResolution resolution = m_specialRedirects.get(page);

        if (resolution != null)
        {
            return resolution.getUrl();
        }

        return null;
    }

    /**
     * <p>
     * Creates a WikiActionBean instance, associates an HTTP request and
     * response with it, and incorporates the correct WikiPage into the bean if
     * required. This method will determine what page the user requested by
     * delegating to
     * {@link #extractPageFromParameter(String, HttpServletRequest)}.
     * </p>
     * <p>
     * This method will <em>always</em>return a WikiActionBean that is
     * properly instantiated. It will also create a new {@WikiActionBeanContext}
     * and associate it with the action bean. The supplied request and response
     * objects will be associated with the WikiActionBeanContext. All three
     * parameters are required, and may not be <code>null</code>.
     * </p>
     * <p>
     * This method performs a similar role to the &lt;stripes:useActionBean&gt;
     * tag, in the sense that it will instantiate an arbitrary WikiActionBean
     * class and, in the case of WikiContext subclasses, bind a WikiPage to it.
     * However, it lacks some of the capabilities the JSP tag possesses. For
     * example, although this method will correctly identity the page requested
     * by the user (by inspecting request parameters), it will not do anything
     * special if the page is a "special page." If special page resolution and
     * redirection is required, use the &lt;stripes:useActionBean&gt; JSP tag
     * instead.
     * </p>
     * 
     * @param request
     *            the HTTP request
     * @param response
     *            the HTTP request
     * @param beanClass
     *            the request context to use by default</code>
     * @return the resolved wiki action bean
     */
    public WikiActionBean newActionBean(HttpServletRequest request, HttpServletResponse response,
                                        Class<? extends WikiActionBean> beanClass) throws WikiException
    {
        if (request == null || response == null)
        {
            throw new IllegalArgumentException("Request or response cannot be null");
        }

        // Try creating a new ActionBean by looking up the request context
        WikiActionBean bean = newInstance(beanClass);

        // OK: we have the correct AbstractActionBean; inject into request scope
        WikiActionBeanContext actionBeanContext = new WikiActionBeanContext();
        actionBeanContext.setRequest(request);
        actionBeanContext.setResponse(response);
        actionBeanContext.setWikiEngine(m_engine);
        bean.setContext(actionBeanContext);

        // If the ActionBean is a WikiContext, extract and set the page (if not
        // null)
        if (bean instanceof WikiContext)
        {
            String page = extractPageFromParameter(request);

            // For view action, default to front page
            if (page == null && bean instanceof ViewActionBean)
            {
                page = m_engine.getFrontPage();
            }
            if (page != null)
            {
                WikiPage wikiPage = resolvePage(request, page);
                ((WikiContext) bean).setPage(wikiPage);
            }
        }
        return bean;
    }

    /**
     * Creates a new ViewActionBean for the given WikiEngine, WikiPage and
     * HttpServletRequest.
     * 
     * @param request
     *            The HttpServletRequest that should be associated with this
     *            context. This parameter may be <code>null</code>.
     * @param response
     *            The HttpServletResponse that should be associated with this
     *            context. This parameter may be <code>null</code>.
     * @param page
     *            The WikiPage. If you want to create a WikiContext for an older
     *            version of a page, you must supply this parameter
     */
    public WikiContext newViewActionBean(HttpServletRequest request, HttpServletResponse response, WikiPage page)
    {
        // Create a new WikiActionBean and set its sevlet context; this will set
        // the WikiEngine too
        WikiActionBeanContext context = new WikiActionBeanContext();
        context.setServletContext(m_engine.getServletContext());

        // If a request or response was passed along, set these references also
        if (request != null)
        {
            context.setRequest(request);
        }
        if (response != null)
        {
            context.setResponse(response);
        }

        // Create a 'view' ActionBean and set the wiki page if passed
        ViewActionBean bean = new ViewActionBean();
        bean.setContext(context);

        // If the page supplied was blank, default to the front page to avoid
        // NPEs
        if (page == null)
        {
            page = m_engine.getPage(m_engine.getFrontPage());

            // Front page does not exist?
            if (page == null)
            {
                page = new WikiPage(m_engine, m_engine.getFrontPage());
            }
        }

        if (page != null)
        {
            bean.setPage(page);
        }
        return bean;
    }

    /**
     * Create a new ViewActionBean for the given WikiPage.
     * 
     * @param page
     *            The WikiPage. If you want to create a WikiContext for an older
     *            version of a page, you must use this constructor.
     */
    public WikiContext newViewActionBean(WikiPage page)
    {
        return newViewActionBean(null, null, page);
    }

    /**
     * <p>
     * Determines the correct wiki page based on a supplied HTTP request. This
     * method attempts to determine the page requested by a user, taking into
     * account special pages. The resolution algorithm will extract the page
     * name from the URL by looking for the first parameter value returned for
     * the <code>page</code> parameter. If a page name was, in fact, passed in
     * the request, this method the correct name after taking into account
     * potential plural matches.
     * </p>
     * <p>
     * If neither of these methods work, or if the request is <code>null</code>
     * this method returns <code>null</code>
     * </p>.
     * 
     * @param request
     *            the HTTP request
     * @return the resolved page name
     */
    protected final String extractPageFromParameter(HttpServletRequest request)
    {
        // Corner case when request == null
        if (request == null)
        {
            return null;
        }

        // Extract the page name from the URL directly
        String[] pages = request.getParameterValues("page");
        String page = null;
        if (pages != null && pages.length > 0)
        {
            page = pages[0];
            try
            {
                // Look for singular/plural variants; if one
                // not found, take the one the user supplied
                String finalPage = getFinalPageName(page);
                if (finalPage != null)
                {
                    page = finalPage;
                }
            }
            catch (ProviderException e)
            {
                // FIXME: Should not ignore!
            }
            return page;
        }

        // Didn't resolve; return null
        return page;
    }

    /**
     * Given an instance of an ActionBean, this method returns a new instance of
     * the same class.
     * 
     * @param beanClass
     *            the bean class that should be newly instantiated
     * @return the newly instantiated bean
     */
    protected WikiActionBean newInstance(Class<? extends WikiActionBean> beanClass) throws WikiException
    {
        if (beanClass != null)
        {
            try
            {
                return beanClass.newInstance();
            }
            catch (Exception e)
            {
                throw new WikiException("Could not create ActionBean: " + e.getMessage());
            }
        }
        return null;
    }

    /**
     * Looks up and returns the correct, versioned WikiPage based on a supplied
     * page name and optional <code>version</code> parameter passed in an HTTP
     * request. If the <code>version</code> parameter does not exist in the
     * request, the latest version is returned.
     * 
     * @param request
     *            the HTTP request
     * @param page
     *            the name of the page to look up; this page <em>must</em>
     *            exist
     * @return the wiki page
     */
    protected final WikiPage resolvePage(HttpServletRequest request, String page)
    {
        // See if the user included a version parameter
        WikiPage wikipage;
        int version = WikiProvider.LATEST_VERSION;
        String rev = request.getParameter("version");

        if (rev != null)
        {
            version = Integer.parseInt(rev);
        }

        wikipage = m_engine.getPage(page, version);

        if (wikipage == null)
        {
            page = MarkupParser.cleanLink(page);
            wikipage = new WikiPage(m_engine, page);
        }
        return wikipage;
    }

    /**
     * Determines whether a "page" exists by examining the list of special pages
     * and querying the page manager.
     * 
     * @param page
     *            the page to seek
     * @return <code>true</code> if the page exists, <code>false</code>
     *         otherwise
     */
    protected final boolean simplePageExists(String page) throws ProviderException
    {
        if (m_specialRedirects.containsKey(page))
        {
            return true;
        }
        return m_engine.getPageManager().pageExists(page);
    }

    /**
     * Returns the WikiActionBean associated with the current
     * {@link javax.servlet.jsp.PageContext}, in request scope. The ActionBean
     * will be retrieved from attribute {@link WikiInterceptor#ATTR_ACTIONBEAN}.
     * 
     * @param pageContext
     *            the
     * @return the WikiActionBean, or <code>null</code> if not found in the
     *         current tag's PageContext
     */
    public static WikiActionBean findActionBean(PageContext pageContext)
    {
        return (WikiActionBean) pageContext.getAttribute(WikiInterceptor.ATTR_ACTIONBEAN, PageContext.REQUEST_SCOPE);
    }

    /**
     * <p>
     * Saves the supplied WikiActionBean and its associated WikiPage as
     * PageContext attributes, in request scope. The action bean and wiki page
     * are saved as attributes named {@link WikiInterceptor#ATTR_ACTIONBEAN} and
     * {@link WikiInterceptor#ATTR_WIKIPAGE}. Among other things, by saving these items as
     * attributes, they can be accessed via JSP Expression Language variables,
     * in this case <code>${wikiActionBean}</code> and
     * <code>${wikiPage}</code> respectively..
     * </p>
     * <p>
     * Note: the WikiPage set by this method is guaranteed to be non-null. If
     * the WikiActionBean is not a WikiContext, or it is a WikiContext but its
     * WikiPage is <code>null</code>, the
     * {@link com.ecyrd.jspwiki.WikiEngine#getFrontPage()} will be consulted,
     * and that page will be used.
     * </p>
     * 
     * @param pageContext
     *            the page context
     * @param actionBean
     *            the WikiActionBean to save
     */
    public static void saveActionBean(PageContext pageContext, WikiActionBean actionBean)
    {
        // Stash the WikiActionBean
        pageContext.setAttribute(WikiInterceptor.ATTR_ACTIONBEAN, actionBean, PageContext.REQUEST_SCOPE);
    
        // Stash the WikiPage
        WikiPage page = null;
        if (actionBean instanceof WikiContext)
        {
            page = ((WikiContext) actionBean).getPage();
        }
        if (page == null)
        {
            // If the page supplied was blank, default to the front page to
            // avoid NPEs
            WikiEngine engine = actionBean.getEngine();
            page = engine.getPage(engine.getFrontPage());
            // Front page does not exist?
            if (page == null)
            {
                page = new WikiPage(engine, engine.getFrontPage());
            }
        }
        if (actionBean instanceof WikiContext)
        {
            ((WikiContext) actionBean).setPage(page);
        }
        pageContext.setAttribute(WikiInterceptor.ATTR_WIKIPAGE, page, PageContext.REQUEST_SCOPE);
    
        // Debug messages
        if (log.isDebugEnabled())
        {
            log.debug("Stashed WikiActionBean '" + actionBean + "' in page scope.");
            log.debug("Stashed WikiPage '" + page.getName() + "' in page scope.");
        }
    
    }

}
