package com.nextmatch.vero.jsonapiadapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nextmatch.vero.jsonapiadapter.internal.GsonAdapter;
import com.nextmatch.vero.jsonapiadapter.internal.JsonApiResponseAdapter;
import com.nextmatch.vero.jsonapiadapter.internal.JsonApiTypeAdapterFactory;
import com.nextmatch.vero.jsonapiadapter.model.Article;
import com.nextmatch.vero.jsonapiadapter.model.Comment;
import com.nextmatch.vero.jsonapiadapter.model.Error;
import com.nextmatch.vero.jsonapiadapter.model.JsonApiParseException;
import com.nextmatch.vero.jsonapiadapter.model.People;
import com.nextmatch.vero.jsonapiadapter.model.SimpleLinks;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author vero
 * @since 2016. 11. 27.
 */
public class JsonApiReaderTest {

    private GsonAdapter _gsonAdapter;

    @Before
    public void setUp() throws Exception {
        Gson gson = new GsonBuilder()
                .registerTypeAdapterFactory(new JsonApiTypeAdapterFactory()).create();

        _gsonAdapter = new GsonAdapter(gson);
    }

    @Test
    public void castResourceToJsonResponseAdapter() throws Exception {
        JsonApiResponseAdapter<Article> responseAdapter = _gsonAdapter.fromJsonApi(JsonApiStrings.simpleSingleResource, Article.class);
        assertNotNull(responseAdapter);
    }

    @Test
    public void resourceIdentifier() throws Exception {
        JsonApiResponseAdapter<Article> responseAdapter = _gsonAdapter.fromJsonApi(JsonApiStrings.simpleSingleResource, Article.class);
        if (responseAdapter.isSuccess()) {
            Article article = responseAdapter.getData();
            assertNotNull(article);
            assertNotNull(article.getIdentifier());
            assertTrue(article.getIdentifier().getId().equals("1"));
            assertTrue(article.getIdentifier().getType().equals("articles"));
        }
    }

    @Test
    public void resourceArrayIdentifier() throws Exception {
        JsonApiResponseAdapter<Article> responseAdapter = _gsonAdapter.fromJsonApi(JsonApiStrings.simpleArrayResource, Article.class);
        if (responseAdapter.isSuccess()) {
            List<Article> articles = responseAdapter.getDataList();
            assertNotNull(articles);
            assertTrue(articles.size() == 1);
        }
    }

    @Test
    public void relationships() throws Exception {
        JsonApiResponseAdapter<Article> responseAdapter = _gsonAdapter.fromJsonApi(JsonApiStrings.simpleArrayRelationshipsResource, Article.class);
        if (responseAdapter.isSuccess()) {
            Article article = responseAdapter.getData();
            assertNotNull(article);
            assertTrue(responseAdapter.hasRelationships("author"));
            assertTrue(responseAdapter.hasRelationships("comments"));
            assertTrue(responseAdapter.hasRelationships(article, "author"));
            assertTrue(responseAdapter.hasRelationships(article, "comments"));
        }
    }

    @Test(expected = JsonApiParseException.class)
    public void relationshipsException() {
        JsonApiResponseAdapter<Article> responseAdapter = _gsonAdapter.fromJsonApi(JsonApiStrings.error, Article.class);
        assertTrue(responseAdapter.hasRelationships("author"));
    }

    @Test
    public void included() throws Exception {
        JsonApiResponseAdapter<Article> responseAdapter = _gsonAdapter.fromJsonApi(JsonApiStrings.included, Article.class);
        Article article = responseAdapter.getData();
        assertTrue(responseAdapter.hasRelationships("comments"));
        List<Comment> comments = responseAdapter.getIncludedCollection(article, "comments", Comment.class);
        assertTrue(comments.size() == 2);
        People author = responseAdapter.getIncluded(article, "author", People.class);
        assertNotNull(author);
        assertFalse(responseAdapter.hasRelationships(author, "author"));
        assertTrue(responseAdapter.hasRelationships(comments.get(0), "author"));
        assertTrue(responseAdapter.hasRelationships(comments.get(1), "author"));
    }

    @Test
    public void links() throws Exception {
        JsonApiResponseAdapter<Article> responseAdapter = _gsonAdapter.fromJsonApi(JsonApiStrings.included, Article.class);
        SimpleLinks rootLinks = responseAdapter.getLinks(SimpleLinks.class);
        Article article = responseAdapter.getData();
        SimpleLinks articleLinks = responseAdapter.getDataLinks(article, SimpleLinks.class);
        SimpleLinks relationshipsLinks = responseAdapter.getRelationshipsLinks(article, "author", SimpleLinks.class);

        assertNotNull(rootLinks);
        assertNotNull(articleLinks);
        assertNotNull(relationshipsLinks);

        People author = responseAdapter.getIncluded(article, "author", People.class);
        SimpleLinks includedLinks = responseAdapter.getIncludedLinks(author, SimpleLinks.class);

        assertNotNull(includedLinks);
    }

    @Test
    public void error() throws Exception {
        JsonApiResponseAdapter<Article> responseAdapter = _gsonAdapter.fromJsonApi(JsonApiStrings.error, Article.class);
        if (!responseAdapter.isSuccess()) {
            List<Error> errors = responseAdapter.getErrors();
            assertNotNull(errors);
            assertTrue(errors.size() == 1);
        }
    }

}
