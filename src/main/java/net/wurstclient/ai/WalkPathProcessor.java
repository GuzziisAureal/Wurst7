/*
 * Copyright (C) 2014 - 2020 | Alexander01998 | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.ai;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.VineBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.wurstclient.WurstClient;
import net.wurstclient.mixinterface.IKeyBinding;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.RotationUtils;

public class WalkPathProcessor extends PathProcessor
{
	public WalkPathProcessor(ArrayList<PathPos> path)
	{
		super(path);
	}
	
	@Override
	public void process()
	{
		// get positions
		BlockPos pos;
		if(WurstClient.MC.player.onGround)
			pos = new BlockPos(WurstClient.MC.player.x,
				WurstClient.MC.player.y + 0.5, WurstClient.MC.player.z);
		else
			pos = new BlockPos(WurstClient.MC.player);
		PathPos nextPos = path.get(index);
		int posIndex = path.indexOf(pos);
		
		if(posIndex == -1)
			ticksOffPath++;
		else
			ticksOffPath = 0;
		
		// update index
		if(pos.equals(nextPos))
		{
			index++;
			
			// disable when done
			if(index >= path.size())
				done = true;
			return;
		}else if(posIndex > index)
		{
			index = posIndex + 1;
			
			// disable when done
			if(index >= path.size())
				done = true;
			return;
		}
		
		lockControls();
		WurstClient.MC.player.abilities.flying = false;
		
		// face next position
		facePosition(nextPos);
		if(MathHelper
			.wrapDegrees(Math.abs(RotationUtils.getHorizontalAngleToLookVec(
				new Vec3d(nextPos).add(0.5, 0.5, 0.5)))) > 90)
			return;
		
		if(WURST.getHax().jesusHack.isEnabled())
		{
			// wait for Jesus to swim up
			if(WurstClient.MC.player.y < nextPos.getY()
				&& (WurstClient.MC.player.isInsideWater()
					|| WurstClient.MC.player.isInLava()))
				return;
			
			// manually swim down if using Jesus
			if(WurstClient.MC.player.y - nextPos.getY() > 0.5
				&& (WurstClient.MC.player.isInsideWater()
					|| WurstClient.MC.player.isInLava()
					|| WURST.getHax().jesusHack.isOverLiquid()))
				((IKeyBinding)MC.options.keySneak).setPressed(true);
		}
		
		// horizontal movement
		if(pos.getX() != nextPos.getX() || pos.getZ() != nextPos.getZ())
		{
			((IKeyBinding)MC.options.keyForward).setPressed(true);
			
			if(index > 0 && path.get(index - 1).isJumping()
				|| pos.getY() < nextPos.getY())
				((IKeyBinding)MC.options.keyJump).setPressed(true);
			
			// vertical movement
		}else if(pos.getY() != nextPos.getY())
			// go up
			if(pos.getY() < nextPos.getY())
			{
				// climb up
				// TODO: Spider
				Block block = BlockUtils.getBlock(pos);
				if(block instanceof LadderBlock || block instanceof VineBlock)
				{
					WURST.getRotationFaker().faceVectorClientIgnorePitch(
						BlockUtils.getBoundingBox(pos).getCenter());
					
					((IKeyBinding)MC.options.keyForward).setPressed(true);
					
				}else
				{
					// directional jump
					if(index < path.size() - 1
						&& !nextPos.up().equals(path.get(index + 1)))
						index++;
					
					// jump up
					((IKeyBinding)MC.options.keyJump).setPressed(true);
				}
				
				// go down
			}else
			{
				// skip mid-air nodes and go straight to the bottom
				while(index < path.size() - 1
					&& path.get(index).down().equals(path.get(index + 1)))
					index++;
				
				// walk off the edge
				if(WurstClient.MC.player.onGround)
					((IKeyBinding)MC.options.keyForward).setPressed(true);
			}
	}
}