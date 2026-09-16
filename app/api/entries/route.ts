import { NextRequest, NextResponse } from 'next/server'
import { isAuthorized } from '@/lib/auth'
import { db } from '@/lib/db'
import { entries, items } from '@/lib/db/schema'
import { and, gte, lt, eq, sql } from 'drizzle-orm'

export async function GET(req: NextRequest) {
  if (!isAuthorized(req)) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 })

  const { searchParams } = new URL(req.url)
  const date = searchParams.get('date')
  const from = searchParams.get('from')
  const to = searchParams.get('to')

  if (date) {
    const start = new Date(`${date}T00:00:00.000Z`)
    const end = new Date(`${date}T00:00:00.000Z`)
    end.setUTCDate(end.getUTCDate() + 1)
    const rows = await db.select().from(entries).where(and(gte(entries.timestamp, start), lt(entries.timestamp, end)))
    return NextResponse.json(rows)
  }

  if (from && to) {
    const start = new Date(`${from}T00:00:00.000Z`)
    const end = new Date(`${to}T00:00:00.000Z`)
    end.setUTCDate(end.getUTCDate() + 1) // `to` is inclusive
    const rows = await db.select().from(entries).where(and(gte(entries.timestamp, start), lt(entries.timestamp, end)))
    return NextResponse.json(rows)
  }

  const all = await db.select().from(entries)
  return NextResponse.json(all)
}

export async function POST(req: NextRequest) {
  if (!isAuthorized(req)) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 })

  const { timestamp, type, itemId, label } = await req.json()
  if (!timestamp || !type) {
    return NextResponse.json({ error: 'timestamp and type are required' }, { status: 400 })
  }
  if ((itemId == null) === (label == null)) {
    return NextResponse.json({ error: 'exactly one of itemId or label is required' }, { status: 400 })
  }

  const [created] = await db.insert(entries).values({ timestamp: new Date(timestamp), type, itemId: itemId ?? null, label: label ?? null }).returning()

  if (itemId != null) {
    await db.update(items).set({ useCount: sql`${items.useCount} + 1`, lastUsedAt: new Date() }).where(eq(items.id, itemId))
  }

  return NextResponse.json(created)
}
