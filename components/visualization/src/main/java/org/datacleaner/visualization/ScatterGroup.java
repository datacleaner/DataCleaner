package org.datacleaner.visualization;

import java.awt.Point;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.datacleaner.api.InputRow;
import org.datacleaner.storage.RowAnnotation;
import org.datacleaner.storage.RowAnnotationFactory;

/**
 * Represents a group of scattered points
 */
public class ScatterGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String _name;

    private final RowAnnotationFactory _rowAnnotationFactory;
    private final Map<Point, RowAnnotation> _annotations = new LinkedHashMap<>();

    public ScatterGroup(String name, RowAnnotationFactory rowAnnotationFactory) {
        _name = name;
        _rowAnnotationFactory = rowAnnotationFactory;
    }

    public String getName() {
        return _name;
    }

    public RowAnnotationFactory getRowAnnotationFactory() {
        return _rowAnnotationFactory;
    }

    public RowAnnotation annotations(Point point) {
        final RowAnnotation annotation = _rowAnnotationFactory.createAnnotation();
        _annotations.put(point, annotation);
        return annotation;
    }

    public void register(Number x, Number y, InputRow row, int distinctCount) {
        final Point point = new Point(x.intValue(), y.intValue());
        final RowAnnotation annotation = annotations(point);
        _rowAnnotationFactory.annotate(row, distinctCount, annotation);
    }

    public RowAnnotation getRowAnnotation(int x, int y) {
        final Point searchedPoint = new Point(x, y);
        return _annotations.get(searchedPoint);
    }

    
    public Set<Point> getCoordinates(){
        return _annotations.keySet();
    }
    
    public Map<Point, RowAnnotation> getAnnotations() {
        return _annotations;
    }

}
