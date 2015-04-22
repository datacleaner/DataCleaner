package org.datacleaner.widgets.result;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.table.DefaultTableModel;

import org.datacleaner.api.Provided;
import org.datacleaner.api.RendererBean;
import org.datacleaner.beans.standardize.Country;
import org.datacleaner.beans.standardize.CountryStandardizationResult;
import org.datacleaner.guice.DCModule;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.result.renderer.SwingRenderingFormat;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.table.DCTable;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Rule;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Symbolizer;
import org.geotools.swing.JMapPane;
import org.jdesktop.swingx.VerticalLayout;
import org.jfree.data.general.DefaultPieDataset;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;

@RendererBean(SwingRenderingFormat.class)
public class CountryStandardizationSwingRenderingBean extends
        CategorizationResultAbstractSwingRenderer<CountryStandardizationResult> {
    private static final float SCALE_HUE_STARTPOINT = 0.4f;
    private static final float SCALE_BRIGHTNESS = 0.9f;
    private static final float SCALE_SATURATION = 0.9f;

    private static final double MAP_ASPECT_RATIO = 0.50;

    private static final Logger logger = LoggerFactory.getLogger(CountryStandardizationSwingRenderingBean.class);

    private static final Color DEFAULT_COUNTRY_BACKGROUND_COLOR = WidgetUtils.BG_COLOR_LESS_BRIGHT;
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    private static final float BACKGROUND_OPACITY = 1.0f;
    private static final Color LINE_COLOR = Color.BLACK;
    private static final float LINE_WIDTH = 1.0f;
    private static final float LINE_OPACITY = 0.2f;
    private static final String GEOM_NAME = "the_geom";

    private final StyleFactory sf = CommonFactoryFinder.getStyleFactory();
    private final FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();

    @Inject
    @Provided
    DCModule dcModule;

    @Override
    public JComponent render(CountryStandardizationResult renderable) {
        final URL shapeUrl = this.getClass().getClassLoader()
                .getResource("geotools/shapefiles/ne_50m_admin_0_countries.shp");

        final FileDataStore store;
        final SimpleFeatureSource featureSource;
        final Map<SimpleFeature, Integer> featureCountMap = new HashMap<>();
        final int maxCount;
        final Envelope2D mapEnvelope;
        CoordinateReferenceSystem crs;
        try {
            store = FileDataStoreFinder.getDataStore(shapeUrl);
            featureSource = store.getFeatureSource();
            crs = featureSource.getSchema().getCoordinateReferenceSystem();
            final List<Geometry> geometries = new ArrayList<>();
            int currentMaxCount = 0;

            for (String country : renderable.getCategoryNames()) {
                SimpleFeature countryFeature = findCountryFeatureId(country, featureSource);
                if (countryFeature == null) {
                    continue;
                }

                int count = renderable.getCategoryCount(country);
                featureCountMap.put(countryFeature, count);
                currentMaxCount = Math.max(count, currentMaxCount);
                geometries.add((Geometry) countryFeature.getDefaultGeometry());
            }

            GeometryFactory factory = JTSFactoryFinder.getGeometryFactory();
            GeometryCollection geometryCollection = (GeometryCollection) factory.buildGeometry(geometries);

            mapEnvelope = JTS.getEnvelope2D(geometryCollection.union().getEnvelope().getEnvelopeInternal(), crs);
            maxCount = currentMaxCount;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        MapContent map = new MapContent();
        map.setTitle("Country Map");

        Style style = createStyle(featureCountMap, maxCount);
        Layer layer = new FeatureLayer(featureSource, style);

        map.addLayer(layer);

        StreamingRenderer renderer = new StreamingRenderer();

        final Map<Key, Object> java2DHintsMap = new HashMap<>();
        java2DHintsMap.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        renderer.setJava2DHints(new RenderingHints(java2DHintsMap));

        final JMapPane mapPane = new JMapPane(map);
        mapPane.setRenderer(renderer);
        mapPane.setBackground(BACKGROUND_COLOR);

        final DCPanel mapPanel = WidgetUtils.decorateWithShadow(mapPane);
        adjustMapPanelHeight(mapPane, mapPanel);

        mapPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                adjustMapPanelHeight(mapPane, e.getComponent());
                mapPane.setDisplayArea(mapEnvelope);
            }

            @Override
            public void componentShown(ComponentEvent e) {
                adjustMapPanelHeight(mapPane, e.getComponent());
            }
        });

        final DefaultPieDataset dataset = new DefaultPieDataset();
        final DefaultTableModel tableModel = prepareModel(renderable, dataset);

        final DCTable countryTable = new DCTable(tableModel);
        countryTable.setColumnControlVisible(false);
        countryTable.setRowHeight(22);
        
        DCPanel panel = new DCPanel();
        panel.setLayout(new VerticalLayout());
        panel.add(mapPanel);
        panel.add(WidgetUtils.decorateWithShadow(countryTable));
        return panel;
    }

    private void adjustMapPanelHeight(JMapPane mapPane, Component parent) {
        mapPane.setPreferredSize(new Dimension(parent.getWidth(), (int) (parent.getWidth() * MAP_ASPECT_RATIO)));
    }

    private SimpleFeature findCountryFeatureId(String countryName, SimpleFeatureSource featureSource) {
        Country country = Country.find(countryName);
        if (country == null) {
            return null;
        }

        final Filter filter;
        final SimpleFeatureCollection features;
        try {
            filter = CQL.toFilter("iso_a3 = '" + country.getThreeLetterISOCode() + "'");
            features = featureSource.getFeatures(filter);
            if (features.isEmpty()) {
                return null;
            }
        } catch (Exception e) {
            logger.warn("Couldn't find country " + countryName, e);
            return null;
        }

        try (final SimpleFeatureIterator featuresIterator = features.features()) {
            return featuresIterator.next();
        }
    }

    private Rule createRule(Color strokeColor, Color fillColor) {
        Fill fill = sf.createFill(ff.literal(fillColor), ff.literal(BACKGROUND_OPACITY));
        Stroke stroke = sf.createStroke(ff.literal(strokeColor), ff.literal(LINE_WIDTH), ff.literal(LINE_OPACITY));
        Symbolizer symbolizer = sf.createPolygonSymbolizer(stroke, fill, GEOM_NAME);

        Rule rule = sf.createRule();
        rule.symbolizers().add(symbolizer);
        return rule;
    }

    private Style createStyle(Map<SimpleFeature, Integer> featureMap, int maxCount) {
        FeatureTypeStyle fts = sf.createFeatureTypeStyle();
        for (Feature feature : featureMap.keySet()) {
            Rule ruleRule = createRule(LINE_COLOR, getColor(featureMap.get(feature), maxCount));
            ruleRule.setFilter(ff.id(feature.getIdentifier()));
            fts.rules().add(ruleRule);
        }

        Rule defaultRule = createRule(LINE_COLOR, DEFAULT_COUNTRY_BACKGROUND_COLOR);
        defaultRule.setElseFilter(true);

        fts.rules().add(defaultRule);

        Style style = sf.createStyle();
        style.featureTypeStyles().add(fts);
        return style;
    }

    private Color getColor(final long count, final long maxCount) {
        float power = count / (float) maxCount;

        float H = SCALE_HUE_STARTPOINT - (power * SCALE_HUE_STARTPOINT);
        float S = SCALE_SATURATION;
        float B = SCALE_BRIGHTNESS;

        return Color.getHSBColor((float) H, (float) S, (float) B);
    }
}
