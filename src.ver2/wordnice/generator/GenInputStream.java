package wordnice.generator;

import java.io.InputStream;


public abstract class GenInputStream
extends InputStream
implements Cloneable {
	
	public GenInputStream() {}
	
	public GenInputStream(Generator gen) {
		if(gen == null) throw new IllegalArgumentException("Generator == null!");
	}
	
	public abstract Generator getGenerator();
	
	/*
	 * (non-Javadoc)
	 * @see java.io.InputStream#read()
	 */
	@Override
	public abstract int read();
	
	/*
	 * (non-Javadoc)
	 * @see java.io.InputStream#read(byte[])
	 */
	@Override
	public int read(byte[] buff) {
		if(buff == null) throw new IllegalArgumentException("Buffer == null!");
		return read(buff, 0, buff.length);
	}
	/*
	 * (non-Javadoc)
	 * @see java.io.InputStream#read(byte[],int,int)
	 */
	@Override
	public abstract int read(byte[] buff, int off, int len);

    /**
     * @see java.io.InputStream#available()
     * @return unlimited (Integer.MAX_VALUE)
     */
    @Override
    public int available() {
        return Integer.MAX_VALUE;
    }

    /*
     * (non-Javadoc)
     * @see java.io.InputStream#close()
     */
    @Override
    public void close() {}

    /*
     * (non-Javadoc)
     * @see java.io.InputStream#mark(int)
     */
	public abstract void mark();
	
	/**
	 * Get marked seed
	 * @return seed or null if not marked yet / mark is not supported
	 */
	public abstract Seed getMarkSeed();
	
	/**
	 * Mark with given seed
	 * @param markSeed Seed to mark
	 * @throws UnsupportedOperationException If mark is not supported
	 * @throws IllegalArgumentException If seed == null
	 */
	public abstract void setMarkSeed(Seed markSeed)throws UnsupportedOperationException;
	
	public Generator cloneMark() {
		return this.getGenerator().cloneFor(this.getMarkSeed());
	}
	
	@Override
	public GenInputStream clone() {
		return this.getGenerator().createStream();
	}
}