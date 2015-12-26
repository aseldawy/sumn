#!/usr/bin/ruby

@r = Random.new
def rand64
  bits = Array.new(64/32).map{(@r.rand * (1 << 32)).to_i}
  bits.pack("L*").unpack("D")
end

puts(@r.rand * 100000)
100000.times { puts(@r.rand)}
