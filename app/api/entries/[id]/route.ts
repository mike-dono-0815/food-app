import { NextRequest, NextResponse } from 'next/server'
import { isAuthorized } from '@/lib/auth'
import { db } from '@/lib/db'
import { entries } from '@/lib/db/schema'
import { eq } from 'drizzle-orm'

export async function PATCH(req: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  if (!isAuthorized(req)) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 })

  const { id } = await params
  const { timestamp } = await req.json()
  if (!timestamp) return NextResponse.json({ error: 'timestamp is required' }, { status: 400 })

  await db.update(entries).set({ timestamp: new Date(timestamp) }).where(eq(entries.id, parseInt(id)))
  return NextResponse.json({ ok: true })
}

export async function DELETE(req: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  if (!isAuthorized(req)) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 })

  const { id } = await params
  await db.delete(entries).where(eq(entries.id, parseInt(id)))
  return NextResponse.json({ ok: true })
}
