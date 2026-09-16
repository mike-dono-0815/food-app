import { NextRequest, NextResponse } from 'next/server'
import { isAuthorized } from '@/lib/auth'
import { db } from '@/lib/db'
import { dailyLogs } from '@/lib/db/schema'
import { and, gte, lte } from 'drizzle-orm'

export async function GET(req: NextRequest) {
  if (!isAuthorized(req)) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 })

  const { searchParams } = new URL(req.url)
  const from = searchParams.get('from')
  const to = searchParams.get('to')
  if (!from || !to) {
    return NextResponse.json({ error: 'from and to are required' }, { status: 400 })
  }

  const rows = await db.select().from(dailyLogs).where(and(gte(dailyLogs.date, from), lte(dailyLogs.date, to)))
  return NextResponse.json(rows)
}
