import { NextRequest, NextResponse } from 'next/server'
import { isAuthorized } from '@/lib/auth'
import { db } from '@/lib/db'
import { dailyLogs, tags } from '@/lib/db/schema'
import { eq } from 'drizzle-orm'

async function homeTagId() {
  const [home] = await db.select().from(tags).where(eq(tags.name, 'Home')).limit(1)
  return home?.id ?? null
}

export async function GET(req: NextRequest, { params }: { params: Promise<{ date: string }> }) {
  if (!isAuthorized(req)) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 })

  const { date } = await params
  const [row] = await db.select().from(dailyLogs).where(eq(dailyLogs.date, date)).limit(1)
  if (row) return NextResponse.json(row)

  // no row yet for this date — synthesize the default shape (not persisted until the first write)
  return NextResponse.json({
    date,
    medicationTakenAt: null,
    wellbeingRating: null,
    wellbeingLoggedAt: null,
    digestionRating: null,
    digestionLoggedAt: null,
    contextTagId: await homeTagId(),
    notes: null,
  })
}

export async function PATCH(req: NextRequest, { params }: { params: Promise<{ date: string }> }) {
  if (!isAuthorized(req)) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 })

  const { date } = await params
  const body = await req.json()

  const updates: Record<string, unknown> = {}
  if ('medicationTakenAt' in body) updates.medicationTakenAt = body.medicationTakenAt ? new Date(body.medicationTakenAt) : null
  if ('wellbeingRating' in body) {
    updates.wellbeingRating = body.wellbeingRating
    updates.wellbeingLoggedAt = new Date()
  }
  if ('digestionRating' in body) {
    updates.digestionRating = body.digestionRating
    updates.digestionLoggedAt = new Date()
  }
  if ('contextTagId' in body) updates.contextTagId = body.contextTagId
  if ('notes' in body) updates.notes = body.notes

  const [existing] = await db.select({ date: dailyLogs.date }).from(dailyLogs).where(eq(dailyLogs.date, date)).limit(1)

  if (existing) {
    await db.update(dailyLogs).set(updates).where(eq(dailyLogs.date, date))
  } else {
    const contextTagId = 'contextTagId' in updates ? (updates.contextTagId as number | null) : await homeTagId()
    await db.insert(dailyLogs).values({ date, ...updates, contextTagId })
  }

  return NextResponse.json({ ok: true })
}
