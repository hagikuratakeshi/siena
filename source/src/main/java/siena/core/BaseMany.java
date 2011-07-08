/**
 * 
 */
package siena.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import siena.PersistenceManager;
import siena.Query;

/**
 * @author mandubian <pascal.voitot@mandubian.org>
 *
 */
public class BaseMany<T> implements Many4PM<T>{
	transient private static final long serialVersionUID = -1417704952199421178L;

	transient protected PersistenceManager pm;
	transient protected Class<T> clazz;
	
	protected RelationMode mode;
	
	transient protected ProxyList<T> list;	
	transient protected Query<T> query;
	
	public BaseMany(PersistenceManager pm, Class<T> clazz){
		this.pm = pm;
		this.clazz = clazz;
		list = new ProxyList<T>(this);
		this.query = pm.createQuery(clazz);
	}
	
	public BaseMany(PersistenceManager pm, Class<T> clazz, RelationMode mode, Object obj, String fieldName) {
		this.pm = pm;
		this.clazz = clazz;
		this.mode = mode;
		list = new ProxyList<T>(this);
		switch(mode){
		case AGGREGATION:
			this.query = pm.createQuery(clazz).aggregated(obj, fieldName);
			break;
		case RELATION:
			this.query = pm.createQuery(clazz).filter(fieldName, obj);
			break;
		}
		query = pm.createQuery(clazz);		
	}

	public SyncList<T> asList() {
		return list.sync();
	}

	public Query<T> asQuery() {
		return query;
	}
	
	public Many4PM<T> setSync(boolean isSync) {
		list.isSync = isSync;
		return this;
	}
	
	public List<T> asList2Remove() {
		return list.elements2Remove;
	}
	
	public List<T> asList2Add() {
		return list.elements2Add;
	}

	public Many4PM<T> aggregationMode(Object aggregator, String fieldName) {
		this.mode = RelationMode.AGGREGATION;
		this.query.release().aggregated(aggregator, fieldName);
		return this;
	}

	public Many4PM<T> relationMode(Object owner, String fieldName) {
		this.mode = RelationMode.RELATION;
		this.query.release().filter(fieldName, owner);
		return this;
	}

	protected class ProxyList<V> implements SyncList<V>{
		transient protected Many<V> many;
		transient protected List<V> elements;
		transient protected List<V> elements2Remove;
		transient protected List<V> elements2Add;

		// isSync set to true by default because when you begin you generally want to add new elements
		transient protected boolean isSync = true;

		public ProxyList(BaseMany<V> many){
			this.many = many;
			this.elements = new ArrayList<V>();
			this.elements2Remove = new ArrayList<V>();
			this.elements2Add = new ArrayList<V>();
		}
		
		public boolean add(V e) {
			elements2Add.add(e);
			return elements.add(e);
		}

		public void add(int index, V element) {
			elements2Add.add(element);
			elements.add(index, element);
		}

		public boolean addAll(Collection<? extends V> c) {
			elements2Add.addAll(c);
			return elements.addAll(c);
		}

		public boolean addAll(int index, Collection<? extends V> c) {
			elements2Add.addAll(c);
			return elements.addAll(index, c);
		}

		public <F extends V> boolean addAll(F... c) {
			List<F> l = Arrays.asList(c);
			elements2Add.addAll(l);
			return elements.addAll(l);
		}

		public <F extends V> boolean addAll(int index, F... c) {
			List<F> l = Arrays.asList(c);
			elements2Add.addAll(l);
			return elements.addAll(index, l);
		}

		public void clear() {
			elements2Add.clear();
			elements2Remove.addAll(elements);
			elements.clear();
		}

		public boolean contains(Object o) {
			return elements.contains(o);
		}

		public boolean containsAll(Collection<?> c) {
			return elements.containsAll(c);
		}

		public V get(int index) {
			return elements.get(index);
		}

		public int indexOf(Object o) {
			return elements.indexOf(o);
		}

		public boolean isEmpty() {
			return elements.isEmpty();
		}

		public Iterator<V> iterator() {
			return elements.iterator();
		}

		public int lastIndexOf(Object o) {
			return elements.lastIndexOf(o);
		}

		public ListIterator<V> listIterator() {
			return elements.listIterator();
		}

		public ListIterator<V> listIterator(int index) {
			return elements.listIterator(index);
		}

		@SuppressWarnings("unchecked")
		public boolean remove(Object o) {
			elements2Remove.add((V)o);
			return elements.remove(o);
		}

		public V remove(int index) {
			V o = elements.remove(index);
			elements2Remove.add(o);
			return o;
		}

		@SuppressWarnings("unchecked")
		public boolean removeAll(Collection<?> c) {
			elements2Remove.addAll((Collection<V>)c);
			return elements.removeAll(c);
		}

		public boolean retainAll(Collection<?> c) {
			return elements.retainAll(c);
		}

		public V set(int index, V element) {
			return elements.set(index, element);
		}

		public int size() {
			return elements.size();
		}

		public List<V> subList(int fromIndex, int toIndex) {
			return elements.subList(fromIndex, toIndex);
		}

		public Object[] toArray() {
			return elements.toArray();
		}

		public <Z> Z[] toArray(Z[] a) {
			return elements.toArray(a);
		}

		public SyncList<V> sync() {
			if(!isSync){
				return forceSync();
			}
			return this; 
		}
		
		public SyncList<V> forceSync() {
			elements = many.asQuery().fetch();
			elements2Remove.clear();
			elements2Add.clear();
			isSync = true;
			return this;
		}
	}


}