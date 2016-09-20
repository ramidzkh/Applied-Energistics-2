/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.client.render.cablebus;


import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import javax.vecmath.Vector3f;

import com.google.common.base.Function;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;

import appeng.api.AEApi;
import appeng.api.util.AEAxisAlignedBB;
import appeng.core.AELog;
import appeng.core.AppEng;


/**
 * Handles creating the quads for facades attached to cable busses.
 */
class FacadeBuilder
{

	static final ResourceLocation TEXTURE_FACADE = new ResourceLocation( AppEng.MOD_ID, "parts/cable_anchor" );

	private final BlockColors blockColors = Minecraft.getMinecraft().getBlockColors();

	private final VertexFormat format;

	private final TextureAtlasSprite facadeTexture;
	private final BlockRendererDispatcher blockRendererDispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();

	FacadeBuilder( VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter )
	{
		this.format = format;
		this.facadeTexture = bakedTextureGetter.apply( TEXTURE_FACADE );
	}

	static Collection<ResourceLocation> getTextures()
	{
		return Collections.singletonList( TEXTURE_FACADE );
	}

	void addFacades( Map<EnumFacing, FacadeRenderState> facadesState, List<AxisAlignedBB> partBoxes, Set<EnumFacing> sidesWithParts, long rand, List<BakedQuad> quads )
	{
		boolean thinFacades = isUseThinFacades( partBoxes );

		CubeBuilder builder = new CubeBuilder( format, quads );

		facadesState.forEach( ( side, textureItem ) ->
		{
			AxisAlignedBB facadeBox = getFacadeBox( side, thinFacades );
			AEAxisAlignedBB cutOutBox = getCutOutBox( facadeBox, partBoxes );
			boolean renderStilt = !sidesWithParts.contains( side );

			try
			{
				addFacade( facadesState, side, cutOutBox, thinFacades, renderStilt, rand, builder );
			}
			catch( Throwable t )
			{
				AELog.debug( t );
			}
		} );
	}

	private void addFacade( Map<EnumFacing, FacadeRenderState> facades, EnumFacing side, AEAxisAlignedBB busBounds, boolean thinFacades, boolean renderStilt, long rand, CubeBuilder builder )
	{

		FacadeRenderState facadeState = facades.get( side );
		IBlockState blockState = facadeState.getSourceBlock();

		builder.setDrawFaces( EnumSet.allOf( EnumFacing.class ) );

		// We only render the stilt if we don't intersect with any part directly, and if there's no part on our side
		if( renderStilt && busBounds == null )
		{
			builder.setTexture( facadeTexture );
			switch( side )
			{
				case DOWN:
					builder.addCube( 7, 1, 7, 9, 6, 9 );
					break;
				case UP:
					builder.addCube( 7, 10, 7, 9, 15, 9 );
					break;
				case NORTH:
					builder.addCube( 7, 7, 1, 9, 9, 6 );
					break;
				case SOUTH:
					builder.addCube( 7, 7, 10, 9, 9, 15 );
					break;
				case WEST:
					builder.addCube( 1, 7, 7, 6, 9, 9 );
					break;
				case EAST:
					builder.addCube( 10, 7, 7, 15, 9, 9 );
					break;
			}
		}

		final float thickness = thinFacades ? 1 : 2;

		IBakedModel blockModel = blockRendererDispatcher.getModelForState( blockState );

		if( AEApi.instance().partHelper().getCableRenderMode().transparentFacades )
		{
			// TODO rbw.setOpacity( 0.3f );
		}

		int color = 0xffffff;
		try
		{
			blockColors.func_189991_a( blockState );
		}
		catch( final Throwable ignored )
		{
		}

		builder.setColorRGB( color );

		// TODO: Cache this
		for( EnumFacing facing : facadeState.getOpenFaces() )
		{
			List<BakedQuad> quads = blockModel.getQuads( blockState, facing, rand );
			for( BakedQuad quad : quads )
			{
				builder.setTexture( quad.getSprite() );
			}
		}

		builder.setDrawFaces( facadeState.getOpenFaces() );

		AxisAlignedBB primaryBox = getFacadeBox( side, thinFacades );

		Vector3f min = new Vector3f(
				(float) primaryBox.minX * 16,
				(float) primaryBox.minY * 16,
				(float) primaryBox.minZ * 16
		);
		Vector3f max = new Vector3f(
				(float) primaryBox.maxX * 16,
				(float) primaryBox.maxY * 16,
				(float) primaryBox.maxZ * 16
		);

		if( busBounds == null )
		{
			// Adjust the facade for neighboring facades so that facade cubes dont overlap with each other
			if( side == EnumFacing.NORTH || side == EnumFacing.SOUTH )
			{
				if( facades.containsKey( EnumFacing.UP ) )
				{
					max.y -= thickness;
				}

				if( facades.containsKey( EnumFacing.DOWN ) )
				{
					min.y += thickness;
				}
			}
			else if( side == EnumFacing.EAST || side == EnumFacing.WEST )
			{
				if( facades.containsKey( EnumFacing.UP ) )
				{
					max.y -= thickness;
				}

				if( facades.containsKey( EnumFacing.DOWN ) )
				{
					min.y += thickness;
				}

				if( facades.containsKey( EnumFacing.SOUTH ) )
				{
					max.z -= thickness;
				}

				if( facades.containsKey( EnumFacing.NORTH ) )
				{
					min.z += thickness;
				}
			}

			builder.addCube( min.x, min.y, min.z, max.x, max.y, max.z );
		}
		else
		{
			Vector3f busMin = new Vector3f( (float) busBounds.minX * 16, (float) busBounds.minY	* 16, (float) busBounds.minZ * 16 );
			Vector3f busMax = new Vector3f( (float) busBounds.maxX * 16, (float) busBounds.maxY	* 16, (float) busBounds.maxZ * 16 );

			if( side == EnumFacing.UP || side == EnumFacing.DOWN )
			{
				this.renderSegmentBlockCurrentBounds( builder, min, max, 0.0f, 0.0f, busMax.z, 16.0f, 16.0f, 16.0f );
				this.renderSegmentBlockCurrentBounds( builder, min, max, 0.0f, 0.0f, 0.0f, 16.0f, 16.0f, busMin.z );
				this.renderSegmentBlockCurrentBounds( builder, min, max, 0.0f, 0.0f, busMin.z, busMin.x, 16.0f, busMax.z );
				this.renderSegmentBlockCurrentBounds( builder, min, max, busMax.x, 0.0f, busMin.z, 16.0f, 16.0f, busMax.z );
			}
			else if( side == EnumFacing.NORTH || side == EnumFacing.SOUTH )
			{
				if( facades.get( EnumFacing.UP ) != null )
				{
					max.y -= thickness;
				}

				if( facades.get( EnumFacing.DOWN ) != null )
				{
					min.y += thickness;
				}

				this.renderSegmentBlockCurrentBounds( builder, min, max, busMax.x, 0.0f, 0.0f, 16.0f, 16.0f, 16.0f );
				this.renderSegmentBlockCurrentBounds( builder, min, max, 0.0f, 0.0f, 0.0f, busMin.x, 16.0f, 16.0f );
				this.renderSegmentBlockCurrentBounds( builder, min, max, busMin.x, 0.0f, 0.0f, busMax.x, busMin.y, 16.0f );
				this.renderSegmentBlockCurrentBounds( builder, min, max, busMin.x, busMax.y, 0.0f, busMax.x, 16.0f, 16.0f );
			}
			else
			{
				if( facades.get( EnumFacing.UP ) != null )
				{
					max.y -= thickness;
				}

				if( facades.get( EnumFacing.DOWN ) != null )
				{
					min.y += thickness;
				}

				if( facades.get( EnumFacing.SOUTH ) != null )
				{
					max.z -= thickness;
				}

				if( facades.get( EnumFacing.NORTH ) != null )
				{
					min.z += thickness;
				}

				this.renderSegmentBlockCurrentBounds( builder, min, max, 0.0f, 0.0f, busMax.z, 16.0f, 16.0f, 16.0f );
				this.renderSegmentBlockCurrentBounds( builder, min, max, 0.0f, 0.0f, 0.0f, 16.0f, 16.0f, busMin.z );
				this.renderSegmentBlockCurrentBounds( builder, min, max, 0.0f, 0.0f, busMin.z, 16.0f, busMin.y, busMax.z );
				this.renderSegmentBlockCurrentBounds( builder, min, max, 0.0f, busMax.y, busMin.z, 16.0f, 16.0f, busMax.z );
			}
		}
	}

	private void renderSegmentBlockCurrentBounds( CubeBuilder builder, Vector3f min, Vector3f max,
			float minX, float minY, float minZ, float maxX, float maxY, float maxZ ) 
	{
		minX = Math.max( min.x, minX );
		minY = Math.max( min.y, minY );
		minZ = Math.max( min.z, minZ );
		maxX = Math.min( max.x, maxX );
		maxY = Math.min( max.y, maxY );
		maxZ = Math.min( max.z, maxZ );

		// don't draw it if its not at least a pixel wide...
		if( maxX - minX >= 1.0 && maxY - minY >= 1.0  && maxZ - minZ >= 1.0 )
		{
			builder.addCube( minX, minY, minZ, maxX, maxY, maxZ );
		}

	}

	/**
	 * Given the actual facade bounding box, and the bounding boxes of all parts, determine the biggest union of AABB that intersect with the
	 * facade's bounding box. This AABB will need to be "cut out" when the facade is rendered.
	 */
	@Nullable
	private static AEAxisAlignedBB getCutOutBox( AxisAlignedBB facadeBox, List<AxisAlignedBB> partBoxes )
	{
		AEAxisAlignedBB b = null;
		for( AxisAlignedBB bb : partBoxes )
		{
			if( bb.intersectsWith( facadeBox ) )
			{
				if( b == null )
				{
					b = AEAxisAlignedBB.fromBounds( bb );
				}
				else
				{
					b.maxX = Math.max( b.maxX, bb.maxX );
					b.maxY = Math.max( b.maxY, bb.maxY );
					b.maxZ = Math.max( b.maxZ, bb.maxZ );
					b.minX = Math.min( b.minX, bb.minX );
					b.minY = Math.min( b.minY, bb.minY );
					b.minZ = Math.min( b.minZ, bb.minZ );
				}
			}
		}
		return b;
	}

	/**
	 * Determines if any of the part's bounding boxes intersects with the outside 2 voxel wide layer.
	 * If so, we should use thinner facades (1 voxel deep).
	 */
	private static boolean isUseThinFacades( List<AxisAlignedBB> partBoxes )
	{
		final double min = 2.0 / 16.0;
		final double max = 14.0 / 16.0;

		for( AxisAlignedBB bb : partBoxes )
		{
			int o = 0;
			o += bb.maxX > max ? 1 : 0;
			o += bb.maxY > max ? 1 : 0;
			o += bb.maxZ > max ? 1 : 0;
			o += bb.minX < min ? 1 : 0;
			o += bb.minY < min ? 1 : 0;
			o += bb.minZ < min ? 1 : 0;

			if( o >= 2 )
			{
				return true;
			}
		}
		return false;
	}

	private static AxisAlignedBB getFacadeBox( EnumFacing side, boolean thinFacades )
	{
		int thickness = thinFacades ? 1 : 2;

		switch( side )
		{
			case DOWN:
				return new AxisAlignedBB( 0.0, 0.0, 0.0, 1.0, ( thickness ) / 16.0, 1.0 );
			case EAST:
				return new AxisAlignedBB( ( 16.0 - thickness ) / 16.0, 0.0, 0.0, 1.0, 1.0, 1.0 );
			case NORTH:
				return new AxisAlignedBB( 0.0, 0.0, 0.0, 1.0, 1.0, ( thickness ) / 16.0 );
			case SOUTH:
				return new AxisAlignedBB( 0.0, 0.0, ( 16.0 - thickness ) / 16.0, 1.0, 1.0, 1.0 );
			case UP:
				return new AxisAlignedBB( 0.0, ( 16.0 - thickness ) / 16.0, 0.0, 1.0, 1.0, 1.0 );
			case WEST:
				return new AxisAlignedBB( 0.0, 0.0, 0.0, ( thickness ) / 16.0, 1.0, 1.0 );
			default:
				throw new IllegalArgumentException( "Unsupported face: " + side );
		}
	}
}
